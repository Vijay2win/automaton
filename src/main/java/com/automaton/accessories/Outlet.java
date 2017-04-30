package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.OutletService;
import com.automaton.characteristics.CharacteristicCallback;

/**
 * A power outlet with boolean power and usage states.
 *
 * @author Andy Lintner
 */
public interface Outlet extends Accessory {

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new OutletService(this));
    }

    /**
     * Retrieves the current binary state of the outlet's power.
     * 
     * @return a future that will contain the binary state
     */
    CompletableFuture<Boolean> getPowerState();

    /**
     * Retrieves the current binary state indicating whether the outlet is in use.
     * 
     * @return a future that will contain the binary state
     */
    CompletableFuture<Boolean> getOutletInUse();

    /**
     * Sets the binary state of the outlet's power.
     * 
     * @param state
     *            the binary state to set
     * @return a future that completes when the change is made
     * @throws Exception
     *             when the change cannot be made
     */
    CompletableFuture<Void> setPowerState(boolean state) throws Exception;

    /**
     * Subscribes to changes in the binary state indicating whether the outlet is in use.
     * 
     * @param callback
     *            the function to call when the state changes.
     */
    void subscribeOutletInUse(CharacteristicCallback callback);
}
