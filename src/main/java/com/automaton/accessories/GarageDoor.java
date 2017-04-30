package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.GarageDoorService;
import com.automaton.characteristics.CharacteristicCallback;

/**
 * A garage door opener, with control and status of a garage door
 *
 * @author Andy Lintner
 */
public interface GarageDoor extends Accessory {
    public enum DoorState {
        OPEN(0), CLOSED(1), OPENING(2), CLOSING(3), STOPPED(4);

        private final int code;

        private DoorState(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static DoorState fromCode(Integer code) {
            return values()[code];
        }
    }

    /**
     * Retrieves the current state of the door
     * 
     * @return a future which will contain the door's state
     */
    CompletableFuture<DoorState> getCurrentDoorState();

    /**
     * Retrieves the targeted state of the door
     * 
     * @return a future which will contain the door's targeted state
     */
    CompletableFuture<DoorState> getTargetDoorState();

    /**
     * Retrieves an indicator of an obstruction detected by the door
     * 
     * @return a future which will contain the indicator
     */
    CompletableFuture<Boolean> getObstructionDetected();

    /**
     * Sets the targeted state of the door.
     * 
     * @param state
     *            the targeted state
     * @return a future that completes when the change is made
     * @throws Exception
     *             when the change cannot be made
     */
    CompletableFuture<Void> setTargetDoorState(DoorState state) throws Exception;

    /**
     * Subscribes to changes in the door's targeted state
     * 
     * @param callback
     *            the function to call when the targeted state changes
     */
    void subscribeTargetDoorState(CharacteristicCallback callback);

    /**
     * Subscribes to changes in the obstruction detected indicator
     * 
     * @param callback
     *            the function to call when the indicator chnages
     */
    void subscribeObstructionDetected(CharacteristicCallback callback);

    /**
     * Unsubscribes from changes in the door's targeted state
     */
    void unsubscribeTargetDoorState();

    /**
     * Unsubscribes from changes in the door's obstruction detected indicator
     */
    void unsubscribeObstructionDetected();

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new GarageDoorService(this));
    }
}
