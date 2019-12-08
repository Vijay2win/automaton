package com.automaton.zwave;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.accessories.Light;
import com.automaton.characteristics.CharacteristicCallback;

public class ZColorfulSwitch extends AbstractZSwitch implements Light.ColorfulLight {
    protected static final Logger logger = LoggerFactory.getLogger(ZColorfulSwitch.class);

    Double brightness = Double.valueOf(0.0D);

    public ZColorfulSwitch(int id, String name) {
        super(id, name);
    }

    public CompletableFuture<Double> getHue() {
        return CompletableFuture.completedFuture(this.brightness);
    }

    public CompletableFuture<Void> setHue(Double value) throws Exception {
        if (value.doubleValue() > 99.0D)
            return CompletableFuture.completedFuture(null);
        this.brightness = value;
        for (CharacteristicCallback callback : this.subscribeCallback)
            callback.changed();
        return CompletableFuture.completedFuture(null);
    }

    public void subscribe(CharacteristicCallback callback) {
        subscribe(callback);
    }

    public void unsubscribe() {
        unsubscribe();
    }

    public CompletableFuture<Double> getSaturation() {
        return getHue();
    }

    public CompletableFuture<Void> setSaturation(Double value) throws Exception {
        return setHue(value);
    }

    public void subscribeSaturation(CharacteristicCallback callback) {
        subscribe(callback);
    }

    public void unsubscribeSaturation() {
        unsubscribe();
    }
}
