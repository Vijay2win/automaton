package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.HumiditySensorService;

/**
 * A humidity sensor that reports the current relative humidity.
 *
 * @author Andy Lintner
 */
public interface HumiditySensor extends Accessory {

    /**
     * Retrieves the current relative humidity.
     * 
     * @return a future that will contain the humidity as a value between 0 and 100
     */
    CompletableFuture<Double> getCurrentRelativeHumidity();

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new HumiditySensorService(this));
    }
}
