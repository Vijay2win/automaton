package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.MotionSensorService;

/**
 * <p>
 * A motion sensor that reports whether motion has been detected.
 * </p>
 *
 * <p>
 * Motion sensors that run on batteries will need to implement this interface and also implement
 * {@link BatteryAccessory}.
 * </p>
 *
 * @author Gaston Dombiak
 */
public interface MotionSensor extends Accessory {

    /**
     * Retrieves the state of the motion sensor. If true then motion has been detected.
     *
     * @return a future that will contain the motion sensor's state
     */
    CompletableFuture<Boolean> getMotionDetected();

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new MotionSensorService(this));
    }
}
