package com.automaton.pairing;

import java.nio.charset.StandardCharsets;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

import com.automaton.http.HttpResponse;
import com.automaton.http.HttpResponses;
import com.automaton.security.*;
import com.automaton.utils.AutomatonUtils;

class FinalPairHandler {
    private final byte[] k;
    private final JmdnsHomekitAdvertiser advertiser;
    private byte[] hkdf_enc_key;

    public FinalPairHandler(byte[] k, JmdnsHomekitAdvertiser advertiser) {
        this.k = k;
        this.advertiser = advertiser;
    }

    public HttpResponse handle(PairSetupRequest req) throws Exception {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        hkdf.init(new HKDFParameters(this.k, "Pair-Setup-Encrypt-Salt".getBytes(StandardCharsets.UTF_8),
                "Pair-Setup-Encrypt-Info".getBytes(StandardCharsets.UTF_8)));
        byte[] okm = this.hkdf_enc_key = new byte[32];
        hkdf.generateBytes(okm, 0, 32);

        return decrypt((PairSetupRequest.Stage3Request) req, okm);
    }

    private HttpResponse decrypt(PairSetupRequest.Stage3Request req, byte[] key) throws Exception {
        ChachaAlgorithm chacha = new ChachaAlgorithm(false, key, "PS-Msg05".getBytes(StandardCharsets.UTF_8));
        byte[] plaintext = chacha.decode(req.getAuthTagData(), req.getMessageData());

        TypeLengthValueUtils.DecodeResult d = TypeLengthValueUtils.decode(plaintext);
        byte[] username = d.getBytes(PairingManager.MessageType.USERNAME);
        byte[] ltpk = d.getBytes(PairingManager.MessageType.PUBLIC_KEY);
        byte[] proof = d.getBytes(PairingManager.MessageType.SIGNATURE);
        return createUser(username, ltpk, proof);
    }

    private HttpResponse createUser(byte[] username, byte[] ltpk, byte[] proof) throws Exception {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        hkdf.init(new HKDFParameters(this.k, "Pair-Setup-Controller-Sign-Salt".getBytes(StandardCharsets.UTF_8),
                "Pair-Setup-Controller-Sign-Info".getBytes(StandardCharsets.UTF_8)));
        byte[] okm = new byte[32];
        hkdf.generateBytes(okm, 0, 32);

        byte[] completeData = AutomatonUtils.joinBytes(new byte[][] { okm, username, ltpk });

        if (!(new EdsaVerifier(ltpk)).verify(completeData, proof)) {
            throw new Exception("Invalid signature");
        }
        Authenticator.INSTANCE.createUser(new String(username, StandardCharsets.UTF_8), ltpk);
        this.advertiser.setDiscoverable(false);
        return createResponse();
    }

    private HttpResponse createResponse() throws Exception {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        hkdf.init(new HKDFParameters(this.k, "Pair-Setup-Accessory-Sign-Salt".getBytes(StandardCharsets.UTF_8),
                "Pair-Setup-Accessory-Sign-Info".getBytes(StandardCharsets.UTF_8)));
        byte[] okm = new byte[32];
        hkdf.generateBytes(okm, 0, 32);

        EdsaSigner signer = new EdsaSigner(Authenticator.INSTANCE.getPrivateKey());
        byte[] material = AutomatonUtils.joinBytes(new byte[][] { okm,
                Authenticator.INSTANCE.getMac().getBytes(StandardCharsets.UTF_8), signer.getPublicKey() });
        byte[] proof = signer.sign(material);

        TypeLengthValueUtils.Encoder encoder = TypeLengthValueUtils.getEncoder();
        encoder.add(PairingManager.MessageType.USERNAME,
                Authenticator.INSTANCE.getMac().getBytes(StandardCharsets.UTF_8));
        encoder.add(PairingManager.MessageType.PUBLIC_KEY, signer.getPublicKey());
        encoder.add(PairingManager.MessageType.SIGNATURE, proof);
        byte[] plaintext = encoder.toByteArray();

        ChachaAlgorithm chacha = new ChachaAlgorithm(true, this.hkdf_enc_key,
                "PS-Msg06".getBytes(StandardCharsets.UTF_8));
        byte[] ciphertext = chacha.encode(plaintext);

        encoder = TypeLengthValueUtils.getEncoder();
        encoder.add(PairingManager.MessageType.STATE, (short) 6);
        encoder.add(PairingManager.MessageType.ENCRYPTED_DATA, ciphertext);

        return (HttpResponse) new HttpResponses.PairingResponse(encoder.toByteArray());
    }
}
