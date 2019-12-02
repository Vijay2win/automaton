package com.automaton.security;

import java.security.*;

import net.i2p.crypto.eddsa.*;
import net.i2p.crypto.eddsa.spec.*;

public class EdsaSigner {
    private final EdDSAPublicKey publicKey;

    public EdsaSigner(byte[] privateKeyBytes) {
        EdDSANamedCurveSpec edDSANamedCurveSpec = EdDSANamedCurveTable.getByName("ed25519-sha-512");
        EdDSAPrivateKeySpec privateKeySpec = new EdDSAPrivateKeySpec(privateKeyBytes,
                (EdDSAParameterSpec) edDSANamedCurveSpec);
        EdDSAPublicKeySpec pubKeySpec = new EdDSAPublicKeySpec(privateKeySpec.getA(),
                (EdDSAParameterSpec) edDSANamedCurveSpec);
        this.publicKey = new EdDSAPublicKey(pubKeySpec);
        this.privateKey = new EdDSAPrivateKey(privateKeySpec);
    }

    private final EdDSAPrivateKey privateKey;

    public byte[] getPublicKey() {
        return this.publicKey.getAbyte();
    }

    public byte[] sign(byte[] material) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        EdDSAEngine edDSAEngine = new EdDSAEngine(MessageDigest.getInstance("SHA-512"));
        edDSAEngine.initSign((PrivateKey) this.privateKey);
        edDSAEngine.update(material);
        return edDSAEngine.sign();
    }
}
