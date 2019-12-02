package com.automaton.security;

import java.io.IOException;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.ChaChaEngine;
import org.bouncycastle.crypto.generators.Poly1305KeyGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.tls.TlsFatalAlert;
import org.bouncycastle.util.Arrays;

public class ChachaAlgorithm {
    private final ChaChaEngine cipher;
    private KeyParameter macKey;

    public ChachaAlgorithm(boolean toEncript, byte[] key, byte[] nonce) throws IOException {
        this.cipher = new ChaChaEngine(20);
        this.cipher.init(toEncript,
                (CipherParameters) new ParametersWithIV((CipherParameters) new KeyParameter(key), nonce));
        this.macKey = initRecordMAC(this.cipher);
    }

    public byte[] decode(byte[] receivedMAC, byte[] ciphertext) throws IOException {
        return decode(receivedMAC, null, ciphertext);
    }

    public byte[] decode(byte[] receivedMAC, byte[] additionalData, byte[] ciphertext) throws IOException {
        byte[] calculatedMAC = PolyKeyCreator.create(this.macKey, additionalData, ciphertext);

        if (!Arrays.constantTimeAreEqual(calculatedMAC, receivedMAC)) {
            throw new TlsFatalAlert((short) 20);
        }
        byte[] output = new byte[ciphertext.length];
        this.cipher.processBytes(ciphertext, 0, ciphertext.length, output, 0);

        return output;
    }

    public byte[] encode(byte[] plaintext) throws IOException {
        return encode(plaintext, null);
    }

    public byte[] encode(byte[] plaintext, byte[] additionalData) throws IOException {
        byte[] ciphertext = new byte[plaintext.length];
        this.cipher.processBytes(plaintext, 0, plaintext.length, ciphertext, 0);

        byte[] calculatedMAC = PolyKeyCreator.create(this.macKey, additionalData, ciphertext);

        byte[] ret = new byte[ciphertext.length + 16];
        System.arraycopy(ciphertext, 0, ret, 0, ciphertext.length);
        System.arraycopy(calculatedMAC, 0, ret, ciphertext.length, 16);
        return ret;
    }

    private KeyParameter initRecordMAC(ChaChaEngine cipher) {
        byte[] firstBlock = new byte[64];
        cipher.processBytes(firstBlock, 0, firstBlock.length, firstBlock, 0);

        System.arraycopy(firstBlock, 0, firstBlock, 32, 16);
        KeyParameter macKey = new KeyParameter(firstBlock, 16, 32);
        Poly1305KeyGenerator.clamp(macKey.getKey());
        return macKey;
    }
}
