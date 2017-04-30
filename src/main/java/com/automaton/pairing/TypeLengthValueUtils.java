package com.automaton.pairing;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.automaton.pairing.PairingManager.MessageType;
import com.automaton.utils.AutomatonUtils;

public class TypeLengthValueUtils {
    public static DecodeResult decode(byte[] content) throws IOException {
        DecodeResult ret = new DecodeResult();
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        while (bais.available() > 0) {
            byte type = (byte) (bais.read() & 0xFF);
            int length = bais.read();
            byte[] part = new byte[length];
            bais.read(part);
            ret.add(type, part);
        }
        return ret;
    }

    public static Encoder getEncoder() {
        return new Encoder();
    }

    public static final class Encoder {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        public void add(MessageType type, BigInteger i) throws IOException {
            add(type, AutomatonUtils.toByteArray(i));
        }

        public void add(MessageType type, short b) {
            baos.write(type.getKey());
            baos.write(1);
            baos.write(b);
        }

        public void add(MessageType type, byte[] bytes) throws IOException {
            InputStream bais = new ByteArrayInputStream(bytes);
            while (bais.available() > 0) {
                int toWrite = bais.available();
                toWrite = toWrite > 255 ? 255 : toWrite;
                baos.write(type.getKey());
                baos.write(toWrite);
                AutomatonUtils.copyStream(bais, baos, toWrite);
            }
        }

        public byte[] toByteArray() {
            return baos.toByteArray();
        }
    }

    public static final class DecodeResult {
        private final Map<Short, byte[]> result = new HashMap<>();

        public byte getByte(MessageType type) {
            return result.get(type.getKey())[0];
        }

        public BigInteger getBigInt(MessageType type) {
            return new BigInteger(1, result.get(type.getKey()));
        }

        public byte[] getBytes(MessageType type) {
            return result.get(type.getKey());
        }

        public void getBytes(MessageType type, byte[] dest, int srcOffset) {
            byte[] b = result.get(type.getKey());
            System.arraycopy(b, srcOffset, dest, 0, Math.min(dest.length, b.length));
        }

        public int getLength(MessageType type) {
            return result.get(type.getKey()).length;
        }

        private void add(short type, byte[] bytes) {
            result.merge(type, bytes, AutomatonUtils::joinBytes);
        }
    }
}
