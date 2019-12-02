package com.automaton.pairing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRegistry;
import com.automaton.http.HttpResponse;
import com.automaton.http.HttpResponses;
import com.automaton.security.*;
import com.automaton.server.FileBasedDB;
import com.automaton.utils.AutomatonUtils;

import djb.Curve25519;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;

public class PairVerificationManager {
    private static final Logger logger = LoggerFactory.getLogger(PairVerificationManager.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    public static final FileBasedDB PAIR_VERIFICATION_DB = new FileBasedDB("pairing-db");
    private final HomekitRegistry registry;

    public static class EncriptionData {
        private byte[] clientPublicKey;
        private byte[] publicKey = new byte[32];
        private byte[] sharedSecret = new byte[32];
        private byte[] hkdfKey = new byte[32];

        public static void serialize(EncriptionData data, ByteBuf stream) throws IOException {
            write(stream, data.clientPublicKey);
            write(stream, data.publicKey);
            write(stream, data.sharedSecret);
            write(stream, data.hkdfKey);
        }

        public static void write(ByteBuf buff, byte[] bytes) throws IOException {
            buff.writeInt(bytes.length);
            buff.writeBytes(bytes);
        }

        public static EncriptionData deserialize(ByteBuf stream) throws IOException {
            EncriptionData data = new EncriptionData();
            data.clientPublicKey = read(stream);
            data.publicKey = read(stream);
            data.sharedSecret = read(stream);
            data.hkdfKey = read(stream);
            return data;
        }

        private static byte[] read(ByteBuf stream) throws IOException {
            byte[] bytes = new byte[stream.readInt()];
            stream.readBytes(bytes);
            return bytes;
        }
    }

    public PairVerificationManager(HomekitRegistry registry) {
        this.registry = registry;
    }

    public HttpResponse handle(FullHttpRequest rawRequest) throws Exception {
        PairVerificationRequest request = PairVerificationRequest
                .of(AutomatonUtils.readAllRemaining(rawRequest.content()));
        switch (request.getStage()) {
        case ONE:
            return stage1((PairVerificationRequest.Stage1Request) request);
        case TWO:
            return stage2((PairVerificationRequest.Stage2Request) request);
        }
        return (HttpResponse) new HttpResponses.NotFoundResponse();
    }

    private HttpResponse stage1(PairVerificationRequest.Stage1Request request) throws Exception {
        EncriptionData data = getEncriptionData();

        logger.info("Starting pair verification for " + registry.getLabel());
        data.clientPublicKey = request.getClientPublicKey();

        data.publicKey = new byte[32];
        byte[] privateKey = new byte[32];
        SECURE_RANDOM.nextBytes(privateKey);
        Curve25519.keygen(data.publicKey, null, privateKey);

        data.sharedSecret = new byte[32];
        Curve25519.curve(data.sharedSecret, privateKey, data.clientPublicKey);

        byte[] material = AutomatonUtils.joinBytes(data.publicKey,
                Authenticator.INSTANCE.getMac().getBytes(StandardCharsets.UTF_8), data.clientPublicKey);
        // AutomatonUtils.joinBytes(new byte[][] { EncriptionData.access$100(data),
        // Authenticator.INSTANCE.getMac().getBytes(StandardCharsets.UTF_8),
        // EncriptionData.access$000(data) });

        byte[] proof = (new EdsaSigner(Authenticator.INSTANCE.getPrivateKey())).sign(material);

        HKDFBytesGenerator hkdf = new HKDFBytesGenerator((Digest) new SHA512Digest());
        hkdf.init((DerivationParameters) new HKDFParameters(data.sharedSecret,
                "Pair-Verify-Encrypt-Salt".getBytes(StandardCharsets.UTF_8),
                "Pair-Verify-Encrypt-Info".getBytes(StandardCharsets.UTF_8)));
        data.hkdfKey = new byte[32];
        hkdf.generateBytes(data.hkdfKey, 0, 32);

        TypeLengthValueUtils.Encoder encoder = TypeLengthValueUtils.getEncoder();
        encoder.add(PairingManager.MessageType.USERNAME,
                Authenticator.INSTANCE.getMac().getBytes(StandardCharsets.UTF_8));
        encoder.add(PairingManager.MessageType.SIGNATURE, proof);

        byte[] plaintext = encoder.toByteArray();
        ChachaAlgorithm chacha = new ChachaAlgorithm(true, data.hkdfKey, "PV-Msg02".getBytes(StandardCharsets.UTF_8));
        byte[] ciphertext = chacha.encode(plaintext);

        encoder = TypeLengthValueUtils.getEncoder();
        encoder.add(PairingManager.MessageType.STATE, (short) 2);
        encoder.add(PairingManager.MessageType.ENCRYPTED_DATA, ciphertext);
        encoder.add(PairingManager.MessageType.PUBLIC_KEY, data.publicKey);

        write(data);
        return (HttpResponse) new HttpResponses.PairingResponse(encoder.toByteArray());
    }

    private EncriptionData getEncriptionData() throws IOException {
        byte[] bytes = PAIR_VERIFICATION_DB.get("instance");
        EncriptionData data = new EncriptionData();
        if (bytes != null)
            data = EncriptionData.deserialize(Unpooled.wrappedBuffer(bytes));
        return data;
    }

    private void write(EncriptionData data) throws IOException {
        ByteBuf buffer = Unpooled.buffer();
        EncriptionData.serialize(data, buffer);
        PAIR_VERIFICATION_DB.put("instance", buffer.array());
    }

    private HttpResponse stage2(PairVerificationRequest.Stage2Request request) throws Exception {
        EncriptionData data = getEncriptionData();

        ChachaAlgorithm chacha = new ChachaAlgorithm(false, data.hkdfKey, "PV-Msg03".getBytes(StandardCharsets.UTF_8));
        byte[] plaintext = chacha.decode(request.getAuthTagData(), request.getMessageData());

        TypeLengthValueUtils.DecodeResult d = TypeLengthValueUtils.decode(plaintext);
        byte[] clientUsername = d.getBytes(PairingManager.MessageType.USERNAME);
        byte[] clientSignature = d.getBytes(PairingManager.MessageType.SIGNATURE);
        byte[] material = AutomatonUtils.joinBytes(data.clientPublicKey, clientUsername, data.publicKey);
        // AutomatonUtils.joinBytes(new byte[][] { EncriptionData.access$000(data),
        // clientUsername, EncriptionData.access$100(data) });
        byte[] clientLtpk = Authenticator.INSTANCE.getUserPublicKey(new String(clientUsername, StandardCharsets.UTF_8));
        if (clientLtpk == null)
            throw new Exception("Unknown user: " + new String(clientUsername, StandardCharsets.UTF_8));

        TypeLengthValueUtils.Encoder encoder = TypeLengthValueUtils.getEncoder();
        if ((new EdsaVerifier(clientLtpk)).verify(material, clientSignature)) {
            encoder.add(PairingManager.MessageType.STATE, (short) 4);
            logger.debug("Completed pair verification for " + this.registry.getLabel());
            return (HttpResponse) new HttpResponses.UpgradeResponse(encoder.toByteArray(),
                    createKey(data, "Control-Write-Encryption-Key"), createKey(data, "Control-Read-Encryption-Key"));
        }
        encoder.add(PairingManager.MessageType.ERROR, (short) 4);
        logger.warn("Invalid signature. Could not pair " + this.registry.getLabel());
        return (HttpResponse) new HttpResponses.OkResponse(encoder.toByteArray());
    }

    private byte[] createKey(EncriptionData data, String info) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator((Digest) new SHA512Digest());
        hkdf.init((DerivationParameters) new HKDFParameters(data.sharedSecret,
                "Control-Salt".getBytes(StandardCharsets.UTF_8), info.getBytes(StandardCharsets.UTF_8)));
        byte[] key = new byte[32];
        hkdf.generateBytes(key, 0, 32);
        return key;
    }
}
