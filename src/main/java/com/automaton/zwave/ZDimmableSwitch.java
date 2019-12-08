package com.automaton.zwave;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.accessories.Light;
import com.automaton.characteristics.CharacteristicCallback;

public class ZDimmableSwitch extends AbstractZSwitch implements Light.DimmableLight {
    protected static final Logger logger = LoggerFactory.getLogger(ZDimmableSwitch.class);

    Integer brightness = Integer.valueOf(0);

    public ZDimmableSwitch(int id, String name) {
        super(id, name);
    }

    public CompletableFuture<Integer> getBrightness() {
        return CompletableFuture.completedFuture(this.brightness);
    }

    public CompletableFuture<Void> setBrightness(Integer value) throws Exception {
        this.brightness = value;
        for (CharacteristicCallback callback : this.subscribeCallback)
            callback.changed();
        return CompletableFuture.completedFuture(null);
    }

    public void subscribe(CharacteristicCallback callback) {
        super.subscribe(callback);
    }

    public void unsubscribe() {
        super.unsubscribe();
    }
}
