package com.automaton.pairing;

import java.math.BigInteger;

import com.nimbusds.srp6.*;

public class HomekitSRP6ServerSession extends SRP6Session {
    public enum State {
        INIT, STEP_1, STEP_2;
    }

    private boolean noSuchUserIdentity = false;
    private BigInteger v = null;
    private BigInteger b = null;

    private State state;

    public HomekitSRP6ServerSession(SRP6CryptoParams config, int timeout) {
        super(timeout);
        if (config == null) {
            throw new IllegalArgumentException("The SRP-6a crypto parameters must not be null");
        }
        this.config = config;
        this.digest = config.getMessageDigestInstance();
        if (this.digest == null) {
            throw new IllegalArgumentException("Unsupported hash algorithm 'H': " + config.H);
        }
        this.state = State.INIT;
        updateLastActivityTime();
    }

    public HomekitSRP6ServerSession(SRP6CryptoParams config) {
        this(config, 0);
    }

    public BigInteger step1(String userID, BigInteger s, BigInteger v) {
        if (userID == null || userID.trim().isEmpty()) {
            throw new IllegalArgumentException("The user identity 'I' must not be null or empty");
        }
        this.userID = userID;

        if (s == null) {
            throw new IllegalArgumentException("The salt 's' must not be null");
        }
        this.s = s;

        if (v == null) {
            throw new IllegalArgumentException("The verifier 'v' must not be null");
        }
        this.v = v;

        if (this.state != State.INIT) {
            throw new IllegalStateException("State violation: Session must be in INIT state");
        }

        this.k = SRP6Routines.computeK(this.digest, this.config.N, this.config.g);
        this.digest.reset();

        this.b = HomekitSRP6Routines.generatePrivateValue(this.config.N, this.random);
        this.digest.reset();

        this.B = SRP6Routines.computePublicServerValue(this.config.N, this.config.g, this.k, v, this.b);

        this.state = State.STEP_1;

        updateLastActivityTime();

        return this.B;
    }

    public BigInteger mockStep1(String userID, BigInteger s, BigInteger v) {
        this.noSuchUserIdentity = true;

        return step1(userID, s, v);
    }

    public BigInteger step2(BigInteger A, BigInteger M1) throws SRP6Exception {
        Object computedM1;
        if (A == null) {
            throw new IllegalArgumentException("The client public value 'A' must not be null");
        }
        this.A = A;

        if (M1 == null) {
            throw new IllegalArgumentException("The client evidence message 'M1' must not be null");
        }
        this.M1 = M1;

        if (this.state != State.STEP_1) {
            throw new IllegalStateException("State violation: Session must be in STEP_1 state");
        }

        if (hasTimedOut()) {
            throw new SRP6Exception("Session timeout", SRP6Exception.CauseType.TIMEOUT);
        }

        if (!SRP6Routines.isValidPublicValue(this.config.N, A)) {
            throw new SRP6Exception("Bad client public value 'A'", SRP6Exception.CauseType.BAD_PUBLIC_VALUE);
        }

        if (this.noSuchUserIdentity) {
            throw new SRP6Exception("Bad client credentials", SRP6Exception.CauseType.BAD_CREDENTIALS);
        }
        if (this.hashedKeysRoutine != null) {
            computedM1 = new URoutineContext(A, this.B);
            this.u = this.hashedKeysRoutine.computeU(this.config, (URoutineContext) computedM1);
        } else {
            this.u = SRP6Routines.computeU(this.digest, this.config.N, A, this.B);
            this.digest.reset();
        }

        this.S = SRP6Routines.computeSessionKey(this.config.N, this.v, this.u, A, this.b);
        if (this.clientEvidenceRoutine != null) {

            SRP6ClientEvidenceContext ctx = new SRP6ClientEvidenceContext(this.userID, this.s, A, this.B, this.S);
            computedM1 = this.clientEvidenceRoutine.computeClientEvidence(this.config, ctx);
        } else {

            computedM1 = SRP6Routines.computeClientEvidence(this.digest, A, this.B, this.S);
            this.digest.reset();
        }

        if (!computedM1.equals(M1)) {
            throw new SRP6Exception("Bad client credentials", SRP6Exception.CauseType.BAD_CREDENTIALS);
        }
        this.state = State.STEP_2;

        if (this.serverEvidenceRoutine != null) {
            SRP6ServerEvidenceContext ctx = new SRP6ServerEvidenceContext(A, M1, this.S);
            this.M2 = this.serverEvidenceRoutine.computeServerEvidence(this.config, ctx);
        }

        updateLastActivityTime();
        return this.M2;
    }

    public State getState() {
        return this.state;
    }
}
