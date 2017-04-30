package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.FanService;
import com.automaton.characteristics.CharacteristicCallback;

/**
 * A fan, with power and rotational characteristics.
 *
 * @author Andy Lintner
 */
public interface Fan extends Accessory {
    public enum RotationDirection {
        CLOCKWISE(0), COUNTER_CLOCKWISE(1);

        private final int code;

        private RotationDirection(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static RotationDirection fromCode(Integer code) {
            return values()[code];
        }
    }

    /**
     * Retrieves the current binary state of the fan's power.
     * 
     * @return a future that will contain the binary state
     */
    CompletableFuture<Boolean> getFanPower();

    /**
     * Retrieves the current rotation direction of the fan.
     * 
     * @return a future that will contain the direction
     */
    CompletableFuture<RotationDirection> getRotationDirection();

    /**
     * Retrieves the current speed of the fan's rotation
     * 
     * @return a future that will contain the speed, expressed as an integer between 0 and 100.
     */
    CompletableFuture<Integer> getRotationSpeed();

    /**
     * Sets the binary state of the fan's power
     * 
     * @param state
     *            the binary state to set
     * @return a future that completes when the change is made
     * @throws Exception
     *             when the change cannot be made
     */
    CompletableFuture<Void> setFanPower(boolean state) throws Exception;

    /**
     * Sets the rotation direction of the fan
     * 
     * @param direction
     *            the direction to set
     * @return a future that completes when the change is made
     * @throws Exception
     *             when the change cannot be made
     */
    CompletableFuture<Void> setRotationDirection(RotationDirection direction) throws Exception;

    /**
     * Sets the speed of the fan's rotation
     * 
     * @param speed
     *            the speed to set, expressed as an integer between 0 and 100.
     * @return a future that completes when the change is made
     * @throws Exception
     *             when the change cannot be made
     */
    CompletableFuture<Void> setRotationSpeed(Integer speed) throws Exception;

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new FanService(this));
    }

    /**
     * Subscribes to changes in the rotation direction of the fan.
     * 
     * @param callback
     *            the function to call when the direction changes.
     */
    void subscribeRotationDirection(CharacteristicCallback callback);

    /**
     * Subscribes to changes in the rotation speed of the fan.
     * 
     * @param callback
     *            the function to call when the speed changes.
     */
    void subscribeRotationSpeed(CharacteristicCallback callback);

    /**
     * Unsubscribes from changes in the rotation direction of the fan.
     */
    void unsubscribeRotationDirection();

    /**
     * Unsubscribes from changes in the fan's rotation speed.
     */
    void unsubscribeRotationSpeed();
}
