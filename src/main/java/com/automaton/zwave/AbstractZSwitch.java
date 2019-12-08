package com.automaton.zwave;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.accessories.Light;
import com.automaton.characteristics.CharacteristicCallback;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

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
        return this.id;
    }

    public String getLabel() {
        return this.label;
    }

    public String getSerialNumber() {
        return this.serial;
    }

    public String getModel() {
        return "on-off-switch";
    }

    public String getManufacturer() {
        return "VJ, Inc.";
    }

    public void identify() {
        System.out.println("Identifying " + this.label);
    }

    public CompletableFuture<Boolean> getPowerState() {
        return CompletableFuture.completedFuture(Boolean.valueOf(this.powerState));
    }

    public CompletableFuture<Void> setPowerState(boolean powerState) throws Exception {
        this.powerState = powerState;
        for (CharacteristicCallback callback : this.subscribeCallback)
            callback.changed();
        return CompletableFuture.completedFuture(null);
    }

    public void subscribe(CharacteristicCallback callback) {
        this.subscribeCallback.add(callback);
    }

    public void unsubscribe() {
        this.subscribeCallback = Sets.filter(this.subscribeCallback, new Predicate<CharacteristicCallback>() {
            public boolean apply(CharacteristicCallback input) {
                return !input.isRemovable();
            }
        });
    }

    public static class ZOnOffSwitch extends AbstractZSwitch implements Light {
        public ZOnOffSwitch(int id, String name) {
            super(id, name);
        }
    }
}
