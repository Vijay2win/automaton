package com.automaton.zwave;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.accessories.Light.DimmableLight;
import com.automaton.characteristics.CharacteristicCallback;

/**
 * This is a big work around since {@link DimmableLight} doesnt work since the characterisitics is not recognized
 * for some reason.
 *
 */
public class ZDimmableSwitch extends AbstractZSwitch implements DimmableLight {
    protected static final Logger logger = LoggerFactory.getLogger(ZDimmableSwitch.class);

    Integer brightness = 0;

    public ZDimmableSwitch(int id, String name) {
        super(id, name);
    }

    @Override
    public CompletableFuture<Integer> getBrightness() {
        return CompletableFuture.completedFuture(brightness);
    }

    @Override
    public CompletableFuture<Void> setBrightness(Integer value) throws Exception {
        this.brightness = value;
        subscribeCallback.forEach(c -> c.changed());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribe(CharacteristicCallback callback) {
        super.subscribe(callback);
    }

    @Override
    public void unsubscribe() {
        super.unsubscribe();        
    }
}
