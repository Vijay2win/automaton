package com.automaton.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nimbusds.srp6.SRP6Routines;

import io.netty.buffer.ByteBuf;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;

public class AutomatonUtils {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();;

    public static BigInteger generateSalt() {
        return new BigInteger(SRP6Routines.generateRandomSalt(16));
    }

    public static byte[] generateKey() throws InvalidAlgorithmParameterException {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName("ed25519-sha-512");
        byte[] seed = new byte[spec.getCurve().getField().getb() / 8];
        SECURE_RANDOM.nextBytes(seed);
        return seed;
    }

    public static String generateMac() {
        int byte1 = ((SECURE_RANDOM.nextInt(255) + 1) | 2) & 0xFE; // Unicast locally administered MAC;
        return Integer.toHexString(byte1) + ":" + Stream.generate(() -> SECURE_RANDOM.nextInt(255) 
                + 1).limit(5).map(i -> Integer.toHexString(i)).collect(Collectors.joining(":"));
    }

    public static byte[] joinBytes(byte[]... piece) {
        int pos = 0;
        int length = 0;
        for (int i = 0; i < piece.length; i++) {
            length += piece[i].length;
        }
        byte[] ret = new byte[length];
        for (int i = 0; i < piece.length; i++) {
            System.arraycopy(piece[i], 0, ret, pos, piece[i].length);
            pos += piece[i].length;
        }
        return ret;
    }

    public static byte[] toByteArray(BigInteger i) {
        byte[] array = i.toByteArray();
        if (array[0] == 0) {
            array = Arrays.copyOfRange(array, 1, array.length);
        }
        return array;
    }

    public static void copyStream(InputStream input, OutputStream output, int length) throws IOException {
        byte[] buffer = new byte[length];
        int remaining = length;
        int bytesRead;
        while ((bytesRead = input.read(buffer, 0, remaining)) != -1 && remaining > 0) {
            output.write(buffer, 0, bytesRead);
            remaining -= bytesRead;
        }
    }

    public static byte[] readAllRemaining(ByteBuf bytes) {
        byte[] ret = new byte[bytes.readableBytes()];
        bytes.readBytes(ret);
        return ret;
    }
}
