package com.automaton.pairing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRegistry;
import com.automaton.http.HttpResponse;
import com.automaton.http.HttpResponses;
import com.automaton.security.Authenticator;
import com.automaton.security.JmdnsHomekitAdvertiser;
import com.automaton.utils.AutomatonUtils;

import io.netty.handler.codec.http.FullHttpRequest;

public class PairingManager {
    private final HomekitRegistry registry;
    private final JmdnsHomekitAdvertiser advertiser;
    private SrpHandler srpHandler;
    private static final Logger logger = LoggerFactory.getLogger(PairingManager.class);

    public enum MessageType {
        METHOD(0), USERNAME(1), SALT(2), PUBLIC_KEY(3), PROOF(4), ENCRYPTED_DATA(5), STATE(6), ERROR(7), SIGNATURE(10);

        private final short key;

        MessageType(short key) {
            this.key = key;
        }

        MessageType(int key) {
            this.key = (short) key;
        }

        public short getKey() {
            return this.key;
        }
    }

    public PairingManager(HomekitRegistry registry, JmdnsHomekitAdvertiser advertiser) {
        this.registry = registry;
        this.advertiser = advertiser;
    }

    public HttpResponse handle(FullHttpRequest httpRequest) throws Exception {
        FinalPairHandler handler;
        PairSetupRequest req = PairSetupRequest.of(AutomatonUtils.readAllRemaining(httpRequest.content()));
        switch (req.getStage()) {
        case ONE:
            logger.info("Starting pair for " + this.registry.getLabel());
            this.srpHandler = new SrpHandler(Authenticator.INSTANCE.getPin(), Authenticator.INSTANCE.getSalt());
            return this.srpHandler.handle(req);
        case TWO:
            logger.debug("Entering second stage of pair for " + this.registry.getLabel());
            if (this.srpHandler == null) {
                logger.warn("Received unexpected stage 2 request for " + this.registry.getLabel());
                return new HttpResponses.UnauthorizedResponse();
            }

            try {
                return this.srpHandler.handle(req);
            } catch (Exception e) {
                this.srpHandler = null;
                logger.error("Exception encountered while processing pairing request", e);
                return (HttpResponse) new HttpResponses.UnauthorizedResponse();
            }
        case THREE:
            logger.debug("Entering third stage of pair for " + this.registry.getLabel());
            if (this.srpHandler == null) {
                logger.warn("Received unexpected stage 3 request for " + this.registry.getLabel());
                return new HttpResponses.UnauthorizedResponse();
            }
            handler = new FinalPairHandler(this.srpHandler.getK(), this.advertiser);
            try {
                return handler.handle(req);
            } catch (Throwable e) {
                logger.error("Exception while finalizing pairing", e);
                return new HttpResponses.UnauthorizedResponse();
            }
        }
        return new HttpResponses.NotFoundResponse();
    }
}
