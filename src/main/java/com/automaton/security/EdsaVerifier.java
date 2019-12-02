package com.automaton.security;

import java.security.MessageDigest;
import java.security.PublicKey;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.*;

public class EdsaVerifier {
    private final PublicKey publicKey;

    public EdsaVerifier(byte[] publicKey) {
        EdDSANamedCurveSpec edDSANamedCurveSpec = EdDSANamedCurveTable.getByName("ed25519-sha-512");
        EdDSAPublicKeySpec pubKey = new EdDSAPublicKeySpec(publicKey, (EdDSAParameterSpec) edDSANamedCurveSpec);
        this.publicKey = (PublicKey) new EdDSAPublicKey(pubKey);
    }

    public boolean verify(byte[] data, byte[] signature) throws Exception {
        EdDSAEngine edDSAEngine = new EdDSAEngine(MessageDigest.getInstance("SHA-512"));
        edDSAEngine.initVerify(this.publicKey);
        edDSAEngine.update(data);

        return edDSAEngine.verify(signature);
    }
}
