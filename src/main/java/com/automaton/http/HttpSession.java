package com.automaton.http;

import java.io.IOException;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRegistry;
import com.automaton.accessories.Accessory;
import com.automaton.pairing.*;
import com.automaton.security.JmdnsHomekitAdvertiser;
import com.automaton.server.SubscriptionManager;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

public class HttpSession {
    private static final Logger logger = LoggerFactory.getLogger(HttpSession.class);

    private volatile PairingManager pairingManager;

    private volatile PairVerificationManager pairVerificationManager;
    private volatile AccessoryController accessoryController;
    private volatile CharacteristicsController characteristicsController;
    private final HomekitRegistry registry;
    private final SubscriptionManager subscriptions;
    private final HomekitConnection connection;
    private final JmdnsHomekitAdvertiser advertiser;

    public HttpSession(HomekitRegistry registry, SubscriptionManager subscriptions, HomekitConnection connection,
            JmdnsHomekitAdvertiser advertiser) {
        this.registry = registry;
        this.subscriptions = subscriptions;
        this.connection = connection;
        this.advertiser = advertiser;
    }

    public HttpResponse handleRequest(FullHttpRequest request) throws IOException {
        switch (request.getUri()) {
        case "/pair-setup":
            return handlePairSetup(request);
        case "/pair-verify":
            return handlePairVerify(request);
        }
        if (this.registry.isAllowUnauthenticatedRequests()) {
            return handleAuthenticatedRequest(request);
        }
        logger.info("Unrecognized request for " + request.getUri());
        return new HttpResponses.NotFoundResponse();
    }

    public HttpResponse handleAuthenticatedRequest(FullHttpRequest request) throws IOException {
        try {
            switch (request.getUri()) {
            case "/accessories":
                return getAccessoryController().listing();
            case "/characteristics":
                if (request.getMethod().equals(HttpMethod.PUT)) {
                    return getCharacteristicsController().put(request, this.connection);
                }
                logger.info("Unrecognized method for " + request.getUri());
                return new HttpResponses.NotFoundResponse();

            case "/pairings":
                return (new PairingUpdateController(this.advertiser)).handle(request);
            }
            if (request.getUri().startsWith("/characteristics?")) {
                return getCharacteristicsController().get(request);
            }
            logger.info("Unrecognized request for " + request.getUri());
            return new HttpResponses.NotFoundResponse();
        } catch (Exception e) {
            logger.error("Could not handle request", e);
            return new HttpResponses.InternalServerErrorResponse(e);
        }
    }

    private HttpResponse handlePairSetup(FullHttpRequest request) {
        if (this.pairingManager == null)
            synchronized (HttpSession.class) {
                if (this.pairingManager == null) {
                    this.pairingManager = new PairingManager(this.registry, this.advertiser);
                }
            }
        try {
            return this.pairingManager.handle(request);
        } catch (Exception e) {
            logger.error("Exception encountered during pairing", e);
            return new HttpResponses.InternalServerErrorResponse(e);
        }
    }

    private HttpResponse handlePairVerify(FullHttpRequest request) {
        if (this.pairVerificationManager == null) {
            synchronized (HttpSession.class) {
                if (this.pairVerificationManager == null) {
                    this.pairVerificationManager = new PairVerificationManager(this.registry);
                }
            }
        }
        try {
            return this.pairVerificationManager.handle(request);
        } catch (Exception e) {
            logger.error("Excepton encountered while verifying pairing", e);
            return new HttpResponses.InternalServerErrorResponse(e);
        }
    }

    private synchronized AccessoryController getAccessoryController() {
        if (this.accessoryController == null)
            this.accessoryController = new AccessoryController(this.registry);
        return this.accessoryController;
    }

    private synchronized CharacteristicsController getCharacteristicsController() {
        if (this.characteristicsController == null)
            this.characteristicsController = new CharacteristicsController(this.registry, this.subscriptions);
        return this.characteristicsController;
    }

    public static class SessionKey {
        private final InetAddress address;
        private final Accessory accessory;

        public SessionKey(InetAddress address, Accessory accessory) {
            this.address = address;
            this.accessory = accessory;
        }

        public boolean equals(Object obj) {
            if (obj instanceof SessionKey) {
                return (this.address.equals(((SessionKey) obj).address)
                        && this.accessory.equals(((SessionKey) obj).accessory));
            }
            return false;
        }

        public int hashCode() {
            int hash = 1;
            hash = hash * 31 + this.address.hashCode();
            hash = hash * 31 + this.accessory.hashCode();
            return hash;
        }
    }
}
