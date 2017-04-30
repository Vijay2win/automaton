package com.automaton.http;

import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.function.Consumer;

import org.bouncycastle.util.Pack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRegistry;
import com.automaton.http.HttpResponses.UpgradeResponse;
import com.automaton.security.ChachaAlgorithm;
import com.automaton.security.JmdnsHomekitAdvertiser;
import com.automaton.server.SubscriptionManager;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;

public class HomekitConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomekitConnection.class);

    private final LengthPrefixedProcessor binaryProcessor = new LengthPrefixedProcessor();
    private final HttpSession httpSession;
    private int inboundBinaryMessageCount = 0;
    private int outboundBinaryMessageCount = 0;
    private byte[] readKey;
    private byte[] writeKey;
    private boolean isUpgraded = false;
    private final Consumer<HttpResponse> messageCallback;
    private final SubscriptionManager subscriptions;

    public HomekitConnection(HomekitRegistry registry, Consumer<HttpResponse> outOfBandMessageCallback, SubscriptionManager subscriptions, JmdnsHomekitAdvertiser advertiser) {
        this.httpSession = new HttpSession(registry, subscriptions, this, advertiser);
        this.messageCallback = outOfBandMessageCallback;
        this.subscriptions = subscriptions;
    }

    public synchronized HttpResponse handleRequest(FullHttpRequest request) throws IOException {
        HttpResponse response = null;
        try {
            response = isUpgraded ? httpSession.handleAuthenticatedRequest(request) : httpSession.handleRequest(request);
            if (response instanceof UpgradeResponse) {
                isUpgraded = true;
                readKey = ((UpgradeResponse) response).getReadKey().array();
                writeKey = ((UpgradeResponse) response).getWriteKey().array();
            }
            return response;
        } finally {
            String stringResponse = response.getBody().hasArray() ? new String(response.getBody().array()) : "";
            LOGGER.info("response code: {}, uri {}", response.getStatusCode(), request.getUri());
            LOGGER.debug("response isUpgraded {}, response string: {}", isUpgraded, stringResponse);
        }
    }

    public byte[] decryptRequest(byte[] ciphertext) {
        if (!isUpgraded)
            throw new RuntimeException("Cannot handle binary before connection is upgraded");

        Collection<byte[]> res = binaryProcessor.handle(ciphertext);
        if (res.isEmpty())
            return new byte[0];

        ByteBuf decrypted = Unpooled.buffer();
        res.stream().map(msg -> decrypt(msg)).forEach(bytes -> decrypted.writeBytes(bytes));
        return decrypted.array();
    }

    public byte[] encryptResponse(byte[] response) throws IOException {
        int offset = 0;
        ByteBuf baos = Unpooled.buffer();
        while (offset < response.length) {
            short length = (short) Math.min(response.length - offset, 0x400);
            byte[] lengthBytes = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(length).array();
            baos.writeBytes(lengthBytes);

            byte[] nonce = Pack.longToLittleEndian(outboundBinaryMessageCount++);
            byte[] plaintext;
            if (response.length == length) {
                plaintext = response;
            } else {
                plaintext = new byte[length];
                System.arraycopy(response, offset, plaintext, 0, length);
            }
            offset += length;
            baos.writeBytes(new ChachaAlgorithm(true, writeKey, nonce).encode(plaintext, lengthBytes));
        }
        return baos.array();
    }

    private byte[] decrypt(byte[] msg) {
        byte[] mac = new byte[16];
        byte[] ciphertext = new byte[msg.length - 16];
        System.arraycopy(msg, 0, ciphertext, 0, msg.length - 16);
        System.arraycopy(msg, msg.length - 16, mac, 0, 16);
        byte[] additionalData = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) (msg.length - 16)).array();
        try {
            byte[] nonce = Pack.longToLittleEndian(inboundBinaryMessageCount++);
            return new ChachaAlgorithm(false, readKey, nonce).decode(mac, additionalData, ciphertext);
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public void close() {
        subscriptions.removeConnection(this);
    }

    public void outOfBand(HttpResponse message) {
        messageCallback.accept(message);
    }
}
