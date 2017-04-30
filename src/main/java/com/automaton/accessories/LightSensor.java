package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.LightSensorService;

/**
 * A light sensor that reports current ambient light level.
 *
 * @author Gaston Dombiak
 */
public interface LightSensor extends Accessory {

    /**
     * Retrieves the current ambient light level.
     *
     * @return a future that will contain the luminance level expressed in LUX.
     */
    CompletableFuture<Double> getCurrentAmbientLightLevel();

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new LightSensorService(this));
    }
}
