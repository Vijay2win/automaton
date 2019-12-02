package com.automaton.pairing;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.http.HttpResponse;
import com.automaton.http.HttpResponses;
import com.nimbusds.srp6.*;

public class SrpHandler {
    private static final BigInteger N_3072 = new BigInteger(
            "5809605995369958062791915965639201402176612226902900533702900882779736177890990861472094774477339581147373410185646378328043729800750470098210924487866935059164371588168047540943981644516632755067501626434556398193186628990071248660819361205119793693985433297036118232914410171876807536457391277857011849897410207519105333355801121109356897459426271845471397952675959440793493071628394122780510124618488232602464649876850458861245784240929258426287699705312584509625419513463605155428017165714465363094021609290561084025893662561222573202082865797821865270991145082200656978177192827024538990239969175546190770645685893438011714430426409338676314743571154537142031573004276428701433036381801705308659830751190352946025482059931306571004727362479688415574702596946457770284148435989129632853918392117997472632693078113129886487399347796982772784615865232621289656944284216824611318709764535152507354116344703769998514148343807");

    private static final BigInteger G = BigInteger.valueOf(5L);

    private static final String IDENTIFIER = "Pair-Setup";
    private static final Logger logger = LoggerFactory.getLogger(SrpHandler.class);

    private final BigInteger salt;
    private final HomekitSRP6ServerSession session;
    private final SRP6CryptoParams config;
    private final String pin;

    public SrpHandler(String pin, BigInteger salt) {
        this.config = new SRP6CryptoParams(N_3072, G, "SHA-512");
        this.session = new HomekitSRP6ServerSession(this.config);
        this.session.setClientEvidenceRoutine(new ClientEvidenceRoutineImpl());
        this.session.setServerEvidenceRoutine(new ServerEvidenceRoutineImpl());
        this.pin = pin;
        this.salt = salt;
    }

    public HttpResponse handle(PairSetupRequest request) throws Exception {
        switch (request.getStage()) {
        case ONE:
            return step1();

        case TWO:
            return step2((PairSetupRequest.Stage2Request) request);
        }

        return (HttpResponse) new HttpResponses.NotFoundResponse();
    }

    private HttpResponse step1() throws Exception {
        if (this.session.getState() != HomekitSRP6ServerSession.State.INIT) {
            logger.error("Session is not in state INIT when receiving step1");
            return (HttpResponse) new HttpResponses.ConflictResponse();
        }

        SRP6VerifierGenerator verifierGenerator = new SRP6VerifierGenerator(this.config);
        verifierGenerator.setXRoutine((XRoutine) new XRoutineWithUserIdentity());
        BigInteger verifier = verifierGenerator.generateVerifier(this.salt, "Pair-Setup", this.pin);

        TypeLengthValueUtils.Encoder encoder = TypeLengthValueUtils.getEncoder();
        encoder.add(PairingManager.MessageType.STATE, (short) 2);
        encoder.add(PairingManager.MessageType.SALT, this.salt);
        encoder.add(PairingManager.MessageType.PUBLIC_KEY, this.session.step1("Pair-Setup", this.salt, verifier));
        return (HttpResponse) new HttpResponses.PairingResponse(encoder.toByteArray());
    }

    private HttpResponse step2(PairSetupRequest.Stage2Request request) throws Exception {
        if (this.session.getState() != HomekitSRP6ServerSession.State.STEP_1) {
            logger.error("Session is not in state Stage 1 when receiving step2");
            return (HttpResponse) new HttpResponses.ConflictResponse();
        }
        BigInteger m2 = this.session.step2(request.getA(), request.getM1());
        TypeLengthValueUtils.Encoder encoder = TypeLengthValueUtils.getEncoder();
        encoder.add(PairingManager.MessageType.STATE, (short) 4);
        encoder.add(PairingManager.MessageType.PROOF, m2);
        return (HttpResponse) new HttpResponses.PairingResponse(encoder.toByteArray());
    }

    public byte[] getK() {
        MessageDigest digest = this.session.getCryptoParams().getMessageDigestInstance();
        BigInteger S = this.session.getSessionKey(false);
        return digest.digest(bigIntegerToUnsignedByteArray(S));
    }

    public static byte[] bigIntegerToUnsignedByteArray(BigInteger i) {
        byte[] array = i.toByteArray();
        if (array[0] == 0)
            array = Arrays.copyOfRange(array, 1, array.length);
        return array;
    }
}
