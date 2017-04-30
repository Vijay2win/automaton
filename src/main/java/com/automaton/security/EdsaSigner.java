package com.automaton.security;

import java.security.*;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.*;

public class EdsaSigner {
    private final EdDSAPublicKey publicKey;
    private final EdDSAPrivateKey privateKey;

    public EdsaSigner(byte[] privateKeyBytes) {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName("ed25519-sha-512");
        EdDSAPrivateKeySpec privateKeySpec = new EdDSAPrivateKeySpec(privateKeyBytes, spec);
        EdDSAPublicKeySpec pubKeySpec = new EdDSAPublicKeySpec(privateKeySpec.getA(), spec);
        this.publicKey = new EdDSAPublicKey(pubKeySpec);
        this.privateKey = new EdDSAPrivateKey(privateKeySpec);
    }

    public byte[] getPublicKey() {
        return publicKey.getAbyte();
    }

    public byte[] sign(byte[] material) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sgr = new EdDSAEngine(MessageDigest.getInstance("SHA-512"));
        sgr.initSign(privateKey);
        sgr.update(material);
        return sgr.sign();
    }
}
