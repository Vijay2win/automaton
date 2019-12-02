package com.automaton.pairing;

import java.math.BigInteger;
import java.security.SecureRandom;

public class HomekitSRP6Routines {
    public static BigInteger generatePrivateValue(BigInteger N, SecureRandom random) {
        int minBits = Math.min(3072, N.bitLength() / 2);
        BigInteger min = BigInteger.ONE.shiftLeft(minBits - 1);
        BigInteger max = N.subtract(BigInteger.ONE);

        return createRandomBigIntegerInRange(min, max, random);
    }

    protected static BigInteger createRandomBigIntegerInRange(BigInteger min, BigInteger max, SecureRandom random) {
        int cmp = min.compareTo(max);
        if (cmp > 0) {
            throw new IllegalArgumentException("'min' may not be greater than 'max'");
        }
        if (cmp == 0) {
            return min;
        }
        if (min.bitLength() > max.bitLength() / 2) {
            return createRandomBigIntegerInRange(BigInteger.ZERO, max.subtract(min), random).add(min);
        }
        int MAX_ITERATIONS = 1000;

        for (int i = 0; i < 1000; i++) {

            BigInteger x = new BigInteger(max.bitLength(), random);

            if (x.compareTo(min) >= 0 && x.compareTo(max) <= 0) {
                return x;
            }
        }

        return (new BigInteger(max.subtract(min).bitLength() - 1, random)).add(min);
    }
}
