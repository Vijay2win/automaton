package com.automaton.pairing;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

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
            ret.add((short) type, part);
        }
        return ret;
    }

    public static Encoder getEncoder() {
        return new Encoder();
    }

    public static final class Encoder {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        public void add(PairingManager.MessageType type, BigInteger i) throws IOException {
            add(type, AutomatonUtils.toByteArray(i));
        }

        public void add(PairingManager.MessageType type, short b) {
            this.baos.write(type.getKey());
            this.baos.write(1);
            this.baos.write(b);
        }

        public void add(PairingManager.MessageType type, byte[] bytes) throws IOException {
            InputStream bais = new ByteArrayInputStream(bytes);
            while (bais.available() > 0) {
                int toWrite = bais.available();
                toWrite = (toWrite > 255) ? 255 : toWrite;
                this.baos.write(type.getKey());
                this.baos.write(toWrite);
                AutomatonUtils.copyStream(bais, this.baos, toWrite);
            }
        }

        public byte[] toByteArray() {
            return this.baos.toByteArray();
        }
    }

    public static final class DecodeResult {
        private final Map<Short, byte[]> result = new HashMap<>();

        public byte getByte(PairingManager.MessageType type) {
            return this.result.get(Short.valueOf(type.getKey()))[0];
        }

        public BigInteger getBigInt(PairingManager.MessageType type) {
            return new BigInteger(1, this.result.get(Short.valueOf(type.getKey())));
        }

        public byte[] getBytes(PairingManager.MessageType type) {
            return this.result.get(Short.valueOf(type.getKey()));
        }

        public void getBytes(PairingManager.MessageType type, byte[] dest, int srcOffset) {
            byte[] b = this.result.get(Short.valueOf(type.getKey()));
            System.arraycopy(b, srcOffset, dest, 0, Math.min(dest.length, b.length));
        }

        public int getLength(PairingManager.MessageType type) {
            return this.result.get(Short.valueOf(type.getKey())).length;
        }

        private void add(short type, byte[] bytes) {
            this.result.merge(Short.valueOf(type), bytes,
                    (xva$0, xva$1) -> AutomatonUtils.joinBytes(new byte[][] { xva$0, xva$1 }));
        }
    }
}
