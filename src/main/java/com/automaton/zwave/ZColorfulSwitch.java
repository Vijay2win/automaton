package com.automaton.zwave;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.accessories.Light.ColorfulLight;
import com.automaton.characteristics.CharacteristicCallback;

public class ZColorfulSwitch extends AbstractZSwitch implements ColorfulLight {
    protected static final Logger logger = LoggerFactory.getLogger(ZColorfulSwitch.class);

    Double brightness = 0d;

    public ZColorfulSwitch(int id, String name) {
        super(id, name);
    }

    @Override
    public CompletableFuture<Double> getHue() {
        return CompletableFuture.completedFuture(brightness);
    }

    @Override
    public CompletableFuture<Void> setHue(Double value) throws Exception {
        if (value > 99)
            return CompletableFuture.completedFuture(null); // ignoring.
        this.brightness = value;
        subscribeCallback.forEach(c -> c.changed());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribe(CharacteristicCallback callback) {
        this.subscribe(callback);
    }

    @Override
    public void unsubscribe() {
        this.unsubscribe();
    }

    @Override
    public CompletableFuture<Double> getSaturation() {
        return getHue(); // work around for now.
    }

    @Override
    public CompletableFuture<Void> setSaturation(Double value) throws Exception {
        return setHue(value); // work around for now.
    }

    @Override
    public void subscribeSaturation(CharacteristicCallback callback) {
        this.subscribe(callback);
    }

    @Override
    public void unsubscribeSaturation() {
        this.unsubscribe();
    }
}
