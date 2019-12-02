package com.automaton.server;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.characteristics.EventableCharacteristic;
import com.automaton.http.*;

public class SubscriptionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionManager.class);

    private final ConcurrentMap<EventableCharacteristic, Set<HomekitConnection>> subscriptions = new ConcurrentHashMap<>();
    private final ConcurrentMap<HomekitConnection, Set<EventableCharacteristic>> reverse = new ConcurrentHashMap<>();

    public synchronized void addSubscription(int aid, int iid, EventableCharacteristic characteristic,
            HomekitConnection connection) {
        synchronized (this) {
            if (!this.subscriptions.containsKey(characteristic)) {
                this.subscriptions.putIfAbsent(characteristic, newSet());
            }
            ((Set<HomekitConnection>) this.subscriptions.get(characteristic)).add(connection);
            if (((Set) this.subscriptions.get(characteristic)).size() == 1) {
                characteristic.subscribe(() -> publish(aid, iid, characteristic));
            }

            if (!this.reverse.containsKey(connection))
                this.reverse.putIfAbsent(connection, newSet());
            ((Set<EventableCharacteristic>) this.reverse.get(connection)).add(characteristic);
            LOGGER.info("Added subscription to " + characteristic.getClass() + " for " + connection.hashCode());
        }
        try {
            connection.outOfBand((new EventController()).getMessage(aid, iid, characteristic));
        } catch (Exception e) {
            LOGGER.error("Could not send initial state in response to subscribe event", e);
        }
    }

    public synchronized void removeSubscription(EventableCharacteristic characteristic, HomekitConnection connection) {
        Set<HomekitConnection> subscriptions = this.subscriptions.get(characteristic);
        if (subscriptions != null) {
            subscriptions.remove(connection);
            if (subscriptions.size() == 0) {
                characteristic.unsubscribe();
            }
        }
        Set<EventableCharacteristic> reverse = this.reverse.get(connection);
        if (reverse != null)
            reverse.remove(characteristic);
        LOGGER.info("Removed subscription to " + characteristic.getClass() + " for " + connection.hashCode());
    }

    public synchronized void removeConnection(HomekitConnection connection) {
        Set<EventableCharacteristic> characteristics = this.reverse.remove(connection);
        if (characteristics == null)
            return;
        for (EventableCharacteristic characteristic : characteristics) {
            Set<HomekitConnection> characteristicSubscriptions = this.subscriptions.get(characteristic);
            characteristicSubscriptions.remove(connection);
            if (characteristicSubscriptions.isEmpty()) {
                characteristic.unsubscribe();
            }
        }
    }

    private <T> Set<T> newSet() {
        return Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public void publish(int accessoryId, int iid, EventableCharacteristic changed) {
        try {
            HttpResponse message = (new EventController()).getMessage(accessoryId, iid, changed);
            LOGGER.info("Publishing changes for " + accessoryId);
            for (HomekitConnection connection : this.subscriptions.get(changed))
                connection.outOfBand(message);
        } catch (Exception e) {
            LOGGER.error("Failed to create new event message", e);
        }
    }
}
