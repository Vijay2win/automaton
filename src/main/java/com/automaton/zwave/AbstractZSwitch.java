package com.automaton.zwave;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.accessories.Light;
import com.automaton.characteristics.CharacteristicCallback;

public abstract class AbstractZSwitch {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractZSwitch.class);

    private final int id;
    private final String serial;
    private final String label;

    private final String model = "on-off-switch";
    private final String manufacturer = "VJ, Inc.";

    protected Set<CharacteristicCallback> subscribeCallback = new HashSet<>();
    public volatile boolean powerState = false;

    public AbstractZSwitch(int id, String name) {
        this.id = id;
        this.serial = "VJ123456." + id;
        this.label = name;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getSerialNumber() {
        return serial;
    }

    public String getModel() {
        return model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void identify() {
        System.out.println("Identifying " + label);
    }

    public CompletableFuture<Boolean> getPowerState() {
        return CompletableFuture.completedFuture(powerState);
    }

    public CompletableFuture<Void> setPowerState(boolean powerState) throws Exception {
        this.powerState = powerState;
        subscribeCallback.forEach(c -> c.changed());
        return CompletableFuture.completedFuture(null);
    }

    public void subscribe(CharacteristicCallback callback) {
        this.subscribeCallback.add(callback);
    }

    public void unsubscribe() {
        this.subscribeCallback = subscribeCallback.stream().filter(i -> !i.isRemovable()).collect(Collectors.toSet());
    }

    public static class ZOnOffSwitch extends AbstractZSwitch implements Light {
        public ZOnOffSwitch(int id, String name) {
            super(id, name);
        }
    }
}
