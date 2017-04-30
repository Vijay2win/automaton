package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.SwitchService;

/**
 * A simple switch with a binary state.
 *
 * @author Andy Lintner
 */
public interface Switch extends Accessory {
    /**
     * Retrieves the current binary state of the switch.
     * 
     * @return a future that will contain the binary state
     */
    CompletableFuture<Boolean> getSwitchState();

    /**
     * Sets the binary state of the switch
     * 
     * @param state
     *            the binary state to set
     * @return a future that completes when the change is made
     * @throws Exception
     *             when the change cannot be made
     */
    CompletableFuture<Void> setSwitchState(boolean state) throws Exception;

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new SwitchService(this));
    }
}
