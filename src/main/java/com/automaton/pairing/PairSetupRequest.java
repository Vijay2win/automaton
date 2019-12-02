package com.automaton.pairing;

import java.math.BigInteger;

abstract class PairSetupRequest {
    private static final short VALUE_STAGE_1 = 1;
    private static final short VALUE_STAGE_2 = 3;
    private static final short VALUE_STAGE_3 = 5;

    public enum Stage {
        ONE, TWO, THREE;
    }

    public static PairSetupRequest of(byte[] content) throws Exception {
        TypeLengthValueUtils.DecodeResult d = TypeLengthValueUtils.decode(content);
        short stage = (short) d.getByte(PairingManager.MessageType.STATE);
        switch (stage) {
        case 1:
            return new Stage1Request();

        case 3:
            return new Stage2Request(d);

        case 5:
            return new Stage3Request(d);
        }

        throw new Exception("Unknown pair process stage: " + stage);
    }

    public abstract Stage getStage();

    public static class Stage1Request extends PairSetupRequest {
        public PairSetupRequest.Stage getStage() {
            return PairSetupRequest.Stage.ONE;
        }
    }

    public static class Stage2Request extends PairSetupRequest {
        private final BigInteger a;
        private final BigInteger m1;

        public Stage2Request(TypeLengthValueUtils.DecodeResult d) {
            this.a = d.getBigInt(PairingManager.MessageType.PUBLIC_KEY);
            this.m1 = d.getBigInt(PairingManager.MessageType.PROOF);
        }

        public BigInteger getA() {
            return this.a;
        }

        public BigInteger getM1() {
            return this.m1;
        }

        public PairSetupRequest.Stage getStage() {
            return PairSetupRequest.Stage.TWO;
        }
    }

    static class Stage3Request extends PairSetupRequest {
        private final byte[] messageData;
        private final byte[] authTagData;

        public Stage3Request(TypeLengthValueUtils.DecodeResult d) {
            this.messageData = new byte[d.getLength(PairingManager.MessageType.ENCRYPTED_DATA) - 16];
            this.authTagData = new byte[16];
            d.getBytes(PairingManager.MessageType.ENCRYPTED_DATA, this.messageData, 0);
            d.getBytes(PairingManager.MessageType.ENCRYPTED_DATA, this.authTagData, this.messageData.length);
        }

        public byte[] getMessageData() {
            return this.messageData;
        }

        public byte[] getAuthTagData() {
            return this.authTagData;
        }

        public PairSetupRequest.Stage getStage() {
            return PairSetupRequest.Stage.THREE;
        }
    }
}
