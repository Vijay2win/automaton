package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.WindowCoveringService;
import com.automaton.characteristics.CharacteristicCallback;

/**
 * A window covering, like blinds, which can be remotely controlled.
 *
 * @author Andy Lintner
 */
public interface WindowCovering extends Accessory {
    public enum PositionState {
        DECREASING(0), INCREASING(1), STOPPED(2);

        private final int code;

        private PositionState(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static PositionState fromCode(Integer code) {
            return values()[code];
        }
    }

    /**
     * Retrieves the current position
     * 
     * @return a future that will contain the position as a value between 0 and 100
     */
    CompletableFuture<Integer> getCurrentPosition();

    /**
     * Retrieves the target position
     * 
     * @return a future that will contain the target position as a value between 0 and 100
     */
    CompletableFuture<Integer> getTargetPosition();

    /**
     * Retrieves the state of the position: increasing, decreasing, or stopped
     * 
     * @return a future that will contain the current state
     */
    CompletableFuture<PositionState> getPositionState();

    /**
     * Retrieves an indication that the window covering is obstructed from moving
     * 
     * @return a future that will contain a boolean indicating whether an obstruction is present
     */
    CompletableFuture<Boolean> getObstructionDetected();

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new WindowCoveringService(this));
    }

    /**
     * Sets the target position
     * 
     * @param position
     *            the target position to set, as a value between 1 and 100
     * @return a future that completes when the change is made
     * @throws Exception
     *             when the change cannot be made
     */
    CompletableFuture<Void> setTargetPosition(int position) throws Exception;

    /**
     * Sets the hold position state
     * 
     * @param hold
     *            whether or not to hold the current position state
     * @return a future that completes when the change is made
     * @throws Exception
     *             when the change cannot be made
     */
    CompletableFuture<Void> setHoldPosition(boolean hold) throws Exception;

    /**
     * Subscribes to changes in the target position.
     * 
     * @param callback
     *            the function to call when the state changes.
     */
    void subscribeTargetPosition(CharacteristicCallback callback);

    /**
     * Subscribes to changes in the position state: increasing, decreasing, or stopped
     * 
     * @param callback
     *            the function to call when the state changes.
     */
    void subscribePositionState(CharacteristicCallback callback);

    /**
     * Subscribes to changes in the obstruction detected state
     * 
     * @param callback
     *            the function to call when the state changes.
     */
    void subscribeObstructionDetected(CharacteristicCallback callback);

    /**
     * Unsubscribes from changes in the target position.
     */
    void unsubscribeTargetPosition();

    /**
     * Unsubscribes from changes in the position state
     */
    void unsubscribePositionState();

    /**
     * Unsubscribes from changes in the obstruction detected state
     */
    void unsubscribeObstructionDetected();

    public interface HorizontalTiltingWindowCovering extends WindowCovering {
        /**
         * Retrieves the current horizontal tilt angle
         * 
         * @return a future that will contain the position as a value between -90 and 90
         */
        CompletableFuture<Integer> getCurrentHorizontalTiltAngle();

        /**
         * Retrieves the target horizontal tilt angle
         * 
         * @return a future that will contain the target position as a value between -90 and 90
         */
        CompletableFuture<Integer> getTargetHorizontalTiltAngle();

        /**
         * Sets the target position
         * 
         * @param angle
         *            the target angle to set, as a value between -90 and 90
         * @return a future that completes when the change is made
         * @throws Exception
         *             when the change cannot be made
         */
        CompletableFuture<Void> setTargetHorizontalTiltAngle(int angle) throws Exception;

        /**
         * Subscribes to changes in the target horizontal tilt angle.
         * 
         * @param callback
         *            the function to call when the state changes.
         */
        void subscribeTargetHorizontalTiltAngle(CharacteristicCallback callback);

        /**
         * Unsubscribes from changes in the target horizontal tilt angle
         */
        void unsubscribeTargetHorizontalTiltAngle();
    }

    public interface VerticalTiltingWindowCovering extends WindowCovering {

        /**
         * Retrieves the current vertical tilt angle
         * 
         * @return a future that will contain the position as a value between -90 and 90
         */
        CompletableFuture<Integer> getCurrentVerticalTiltAngle();

        /**
         * Retrieves the target vertical tilt angle
         * 
         * @return a future that will contain the target position as a value between -90 and 90
         */
        CompletableFuture<Integer> getTargetVerticalTiltAngle();

        /**
         * Sets the target position
         * 
         * @param angle
         *            the target angle to set, as a value between -90 and 90
         * @return a future that completes when the change is made
         * @throws Exception
         *             when the change cannot be made
         */
        CompletableFuture<Void> setTargetVerticalTiltAngle(int angle) throws Exception;

        /**
         * Subscribes to changes in the target vertical tilt angle.
         * 
         * @param callback
         *            the function to call when the state changes.
         */
        void subscribeTargetVerticalTiltAngle(CharacteristicCallback callback);

        /**
         * Unsubscribes from changes in the target vertical tilt angle
         */
        void unsubscribeTargetVerticalTiltAngle();
    }
}
