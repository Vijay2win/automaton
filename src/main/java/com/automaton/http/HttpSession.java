package com.automaton.http;

import java.io.IOException;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRegistry;
import com.automaton.accessories.Accessory;
import com.automaton.http.HttpResponses.InternalServerErrorResponse;
import com.automaton.http.HttpResponses.NotFoundResponse;
import com.automaton.pairing.PairVerificationManager;
import com.automaton.pairing.PairingManager;
import com.automaton.pairing.PairingUpdateController;
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

    public HttpSession(HomekitRegistry registry, SubscriptionManager subscriptions, HomekitConnection connection, JmdnsHomekitAdvertiser advertiser) {
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
        default:
            if (registry.isAllowUnauthenticatedRequests()) {
                return handleAuthenticatedRequest(request);
            } else {
                logger.info("Unrecognized request for " + request.getUri());
                return new NotFoundResponse();
            }
        }
    }

    public HttpResponse handleAuthenticatedRequest(FullHttpRequest request) throws IOException {
        try {
            switch (request.getUri()) {
            case "/accessories":
                return getAccessoryController().listing();
            case "/characteristics":
                if (request.getMethod().equals(HttpMethod.PUT)) {
                    return getCharacteristicsController().put(request, connection);
                } else {
                    logger.info("Unrecognized method for " + request.getUri());
                    return new NotFoundResponse();
                }
            case "/pairings":
                return new PairingUpdateController(advertiser).handle(request);
            default:
                if (request.getUri().startsWith("/characteristics?"))
                    return getCharacteristicsController().get(request);
            }
            logger.info("Unrecognized request for " + request.getUri());
            return new NotFoundResponse();
        } catch (Exception e) {
            logger.error("Could not handle request", e);
            return new InternalServerErrorResponse(e);
        }
    }

    private HttpResponse handlePairSetup(FullHttpRequest request) {
        if (pairingManager == null) {
            synchronized (HttpSession.class) {
                if (pairingManager == null)
                    pairingManager = new PairingManager(registry, advertiser);
            }
        }
        try {
            return pairingManager.handle(request);
        } catch (Exception e) {
            logger.error("Exception encountered during pairing", e);
            return new InternalServerErrorResponse(e);
        }
    }

    private HttpResponse handlePairVerify(FullHttpRequest request) {
        if (pairVerificationManager == null) {
            synchronized (HttpSession.class) {
                if (pairVerificationManager == null) {
                    pairVerificationManager = new PairVerificationManager(registry);
                }
            }
        }
        try {
            return pairVerificationManager.handle(request);
        } catch (Exception e) {
            logger.error("Excepton encountered while verifying pairing", e);
            return new InternalServerErrorResponse(e);
        }
    }

    private synchronized AccessoryController getAccessoryController() {
        if (accessoryController == null)
            accessoryController = new AccessoryController(registry);
        return accessoryController;
    }

    private synchronized CharacteristicsController getCharacteristicsController() {
        if (characteristicsController == null)
            characteristicsController = new CharacteristicsController(registry, subscriptions);
        return characteristicsController;
    }

    public static class SessionKey {
        private final InetAddress address;
        private final Accessory accessory;

        public SessionKey(InetAddress address, Accessory accessory) {
            this.address = address;
            this.accessory = accessory;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SessionKey) {
                return address.equals(((SessionKey) obj).address) && accessory.equals(((SessionKey) obj).accessory);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 1;
            hash = hash * 31 + address.hashCode();
            hash = hash * 31 + accessory.hashCode();
            return hash;
        }
    }

}
