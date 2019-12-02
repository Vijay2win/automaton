package com.automaton.pairing;

abstract class PairVerificationRequest {
    private static final short VALUE_STAGE_1 = 1;
    private static final short VALUE_STAGE_2 = 3;

    static PairVerificationRequest of(byte[] content) throws Exception {
        TypeLengthValueUtils.DecodeResult d = TypeLengthValueUtils.decode(content);
        short stage = (short) d.getByte(PairingManager.MessageType.STATE);
        switch (stage) {
        case 1:
            return new Stage1Request(d);

        case 3:
            return new Stage2Request(d);
        }

        throw new Exception("Unknown pair process stage: " + stage);
    }

    abstract PairSetupRequest.Stage getStage();

    static class Stage1Request extends PairVerificationRequest {
        private final byte[] clientPublicKey;

        public Stage1Request(TypeLengthValueUtils.DecodeResult d) {
            this.clientPublicKey = d.getBytes(PairingManager.MessageType.PUBLIC_KEY);
        }

        public byte[] getClientPublicKey() {
            return this.clientPublicKey;
        }

        PairSetupRequest.Stage getStage() {
            return PairSetupRequest.Stage.ONE;
        }
    }

    static class Stage2Request extends PairVerificationRequest {
        private final byte[] messageData;
        private final byte[] authTagData;

        public Stage2Request(TypeLengthValueUtils.DecodeResult d) {
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
            return PairSetupRequest.Stage.TWO;
        }
    }
}
