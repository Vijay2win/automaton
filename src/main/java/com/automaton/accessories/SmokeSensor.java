package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.SmokeSensorService;

/**
 * <p>
 * A smoke sensor reports whether smoke has been detected or not.
 * </p>
 *
 * <p>
 * Smoke sensors that run on batteries will need to implement this interface and also implement {@link BatteryAccessory}
 * .
 * </p>
 *
 * @author Gaston Dombiak
 */
public interface SmokeSensor extends Accessory {
    public enum SmokeDetectedState {
        NOT_DETECTED(0), DETECTED(1);

        private final int code;

        SmokeDetectedState(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static SmokeDetectedState fromCode(Integer code) {
            return values()[code];
        }
    }

    /**
     * Retrieves the state of the smoke sensor. This is whether smoke has been detected or not.
     *
     * @return a future that will contain the smoke sensor's state
     */
    CompletableFuture<SmokeDetectedState> getSmokeDetectedState();

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new SmokeSensorService(this));
    }
}
