package com.automaton.http;

import java.util.function.Consumer;

import com.automaton.HomekitRegistry;
import com.automaton.security.JmdnsHomekitAdvertiser;
import com.automaton.server.SubscriptionManager;

public class HomekitConnectionFactory {
    private final HomekitRegistry registry;
    private final SubscriptionManager subscriptions;
    private final JmdnsHomekitAdvertiser advertiser;

    public HomekitConnectionFactory(HomekitRegistry registry, SubscriptionManager subscriptions, JmdnsHomekitAdvertiser advertiser) {
        this.registry = registry;
        this.subscriptions = subscriptions;
        this.advertiser = advertiser;
    }

    public HomekitConnection createConnection(Consumer<HttpResponse> outOfBandMessageCallback) {
        return new HomekitConnection(registry, outOfBandMessageCallback, subscriptions, advertiser);
    }
}
