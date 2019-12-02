package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.CarbonMonoxideSensorService;

public interface CarbonMonoxideSensor extends Accessory {
    public enum CarbonMonoxideDetectedState {
        NORMAL(0), ABNORMAL(1);

        private final int code;

        CarbonMonoxideDetectedState(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    CompletableFuture<CarbonMonoxideDetectedState> getCarbonMonoxideDetectedState();

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new CarbonMonoxideSensorService(this));
    }
}
