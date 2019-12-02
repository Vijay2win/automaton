package com.automaton;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.accessories.Accessory;
import com.automaton.http.HomekitConnectionFactory;
import com.automaton.http.NettyHttpServer;
import com.automaton.security.Authenticator;
import com.automaton.security.JmdnsHomekitAdvertiser;
import com.automaton.server.SubscriptionManager;

public class HomekitRoot {
    private static final Logger logger = LoggerFactory.getLogger(HomekitRoot.class);

    private final JmdnsHomekitAdvertiser advertiser;
    private final NettyHttpServer webHandler;
    private final String label;
    private final HomekitRegistry registry;
    private final SubscriptionManager subscriptions = new SubscriptionManager();
    private boolean started = false;
    private int configurationIndex = 1;

    public HomekitRoot(String label, NettyHttpServer webHandler, InetAddress localhost) throws IOException {
        this(label, webHandler, new JmdnsHomekitAdvertiser(localhost));
    }

    public HomekitRoot(String label, NettyHttpServer webHandler, JmdnsHomekitAdvertiser advertiser) throws IOException {
        this.advertiser = advertiser;
        this.webHandler = webHandler;
        this.label = label;
        this.registry = new HomekitRegistry(label);
    }

    public void addAccessory(Accessory accessory) {
        if (accessory.getId() <= 1 && !(accessory instanceof HomekitBridge)) {
            throw new IndexOutOfBoundsException("The ID of an accessory used in a bridge must be greater than 1");
        }
        addAccessorySkipRangeCheck(accessory);
    }

    void addAccessorySkipRangeCheck(Accessory accessory) {
        this.registry.add(accessory);
        logger.info("Added accessory " + accessory.getLabel());
        if (this.started) {
            this.registry.reset();
            this.webHandler.resetConnections();
        }
    }

    public void removeAccessory(Accessory accessory) {
        this.registry.remove(accessory);
        logger.info("Removed accessory " + accessory.getLabel());
        if (this.started) {
            this.registry.reset();
            this.webHandler.resetConnections();
        }
    }

    public void start() throws InterruptedException, ExecutionException {
        this.started = true;
        this.registry.reset();
        CompletableFuture<Void> future = this.webHandler
                .start(new HomekitConnectionFactory(this.registry, this.subscriptions, this.advertiser))
                .thenAccept(port -> {
                    try {
                        refreshAuthInfo();
                        this.advertiser.advertise(this.label, Authenticator.INSTANCE.getMac(), port.intValue(),
                                this.configurationIndex);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        future.get();
    }

    public void stop() {
        this.advertiser.stop();
        this.webHandler.shutdown();
        this.started = false;
    }

    public void refreshAuthInfo() throws IOException {
        this.advertiser.setDiscoverable(true);
    }

    public void allowUnauthenticatedRequests(boolean allow) {
        this.registry.setAllowUnauthenticatedRequests(allow);
    }

    public void setConfigurationIndex(int revision) throws IOException {
        if (revision < 1)
            throw new IllegalArgumentException("revision must be greater than or equal to 1");
        this.configurationIndex = revision;
        if (this.started) {
            this.advertiser.setConfigurationIndex(revision);
        }
    }

    public HomekitRegistry getRegistry() {
        return this.registry;
    }
}
