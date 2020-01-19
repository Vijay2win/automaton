package com.automaton.http;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.function.Consumer;

import org.bouncycastle.util.Pack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRegistry;
import com.automaton.security.ChachaAlgorithm;
import com.automaton.security.JmdnsHomekitAdvertiser;
import com.automaton.server.SubscriptionManager;

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

    public HomekitConnection(HomekitRegistry registry, Consumer<HttpResponse> outOfBandMessageCallback,
            SubscriptionManager subscriptions, JmdnsHomekitAdvertiser advertiser) {
        this.httpSession = new HttpSession(registry, subscriptions, this, advertiser);
        this.messageCallback = outOfBandMessageCallback;
        this.subscriptions = subscriptions;
    }

    public HttpResponse handleRequest(FullHttpRequest request) throws IOException {
        synchronized (binaryProcessor) {
            return doHandleRequest(request);
        }
    }

    private HttpResponse doHandleRequest(FullHttpRequest request) throws IOException {
        HttpResponse response = this.isUpgraded ? this.httpSession.handleAuthenticatedRequest(request)
                : this.httpSession.handleRequest(request);
        if (response instanceof HttpResponses.UpgradeResponse) {
            this.isUpgraded = true;
            this.readKey = ((HttpResponses.UpgradeResponse) response).getReadKey().array();
            this.writeKey = ((HttpResponses.UpgradeResponse) response).getWriteKey().array();
        }
        LOGGER.info(response.getStatusCode() + " " + request.getUri());
        return response;
    }

    public byte[] decryptRequest(byte[] ciphertext) {
        if (!this.isUpgraded) {
            throw new RuntimeException("Cannot handle binary before connection is upgraded");
        }
        Collection<byte[]> res = binaryProcessor.handle(ciphertext);
        if (res.isEmpty()) {
            return new byte[0];
        }
        try (ByteArrayOutputStream decrypted = new ByteArrayOutputStream()) {
            res.stream().map(msg -> decrypt(msg)).forEach(bytes -> {
                try {
                    decrypted.write(bytes);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return decrypted.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encryptResponse(byte[] response) throws IOException {
        int offset = 0;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            while (offset < response.length) {
                byte[] plaintext;
                short length = (short) Math.min(response.length - offset, 1024);
                byte[] lengthBytes = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(length).array();
                baos.write(lengthBytes);

                byte[] nonce = Pack.longToLittleEndian(this.outboundBinaryMessageCount++);

                if (response.length == length) {
                    plaintext = response;
                } else {
                    plaintext = new byte[length];
                    System.arraycopy(response, offset, plaintext, 0, length);
                }
                offset += length;
                baos.write(new ChachaAlgorithm(true, this.writeKey, nonce).encode(plaintext, lengthBytes));
            }
            return baos.toByteArray();
        }
    }

    private byte[] decrypt(byte[] msg) {
        byte[] mac = new byte[16];
        byte[] ciphertext = new byte[msg.length - 16];
        System.arraycopy(msg, 0, ciphertext, 0, msg.length - 16);
        System.arraycopy(msg, msg.length - 16, mac, 0, 16);
        byte[] additionalData = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short) (msg.length - 16)).array();
        try {
            byte[] nonce = Pack.longToLittleEndian(this.inboundBinaryMessageCount++);
            return new ChachaAlgorithm(false, this.readKey, nonce).decode(mac, additionalData, ciphertext);
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public void close() {
        this.subscriptions.removeConnection(this);
    }

    public void outOfBand(HttpResponse message) {
        this.messageCallback.accept(message);
    }
}
