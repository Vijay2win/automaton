package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.SecuritySystemService;
import com.automaton.characteristics.CharacteristicCallback;

/**
 * <p>
 * A security system that can be armed so that when a contact sensor is opened or a motion sensor detects movement, then
 * a siren could be fired off. There are different modes for arming the system. See {@link TargetSecuritySystemState}
 * for more information.
 * </p>
 *
 * @author Gaston Dombiak
 */
public interface SecuritySystem extends Accessory {
    public enum SecuritySystemAlarmType {
        CLEARED(0), UNKNOWN(1);

        private final int code;

        SecuritySystemAlarmType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static SecuritySystemAlarmType fromCode(Integer code) {
            return values()[code];
        }
    }

    public enum CurrentSecuritySystemState {
        STAY_ARM(0), AWAY_ARM(1), NIGHT_ARM(2), DISARMED(3), TRIGGERED(4);

        private final int code;

        CurrentSecuritySystemState(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static CurrentSecuritySystemState fromCode(Integer code) {
            return values()[code];
        }
    }

    public enum TargetSecuritySystemState {
        STAY_ARM(0), AWAY_ARM(1), NIGHT_ARM(2), DISARMED(3);

        private final int code;

        TargetSecuritySystemState(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static TargetSecuritySystemState fromCode(Integer code) {
            return values()[code];
        }
    }

    /**
     * Retrieves the current state of the security system. The state describes if the system is armed in any of its
     * variations; or if the alarm has been triggered; or if the system is disarmed.
     *
     * @return current state of the security system.
     */
    CompletableFuture<CurrentSecuritySystemState> getCurrentSecuritySystemState();

    /**
     * Subscribes to changes to the state of the security system.
     *
     * @param callback
     *            the function to call when the state changes.
     */
    void subscribeCurrentSecuritySystemState(CharacteristicCallback callback);

    /**
     * Unsubscribes from changes in the state of the security system.
     */
    void unsubscribeCurrentSecuritySystemState();

    /**
     * Sets the state of the security system. The security system could be armed in any of its variations or disarmed.
     *
     * @param state
     *            target state of the security system.
     * @throws Exception
     *             when the change cannot be made.
     */
    void setTargetSecuritySystemState(TargetSecuritySystemState state) throws Exception;

    /**
     * Retrieves the pending, but not yet completed, state of the security system.
     *
     * @return target state of the security system.
     */
    CompletableFuture<TargetSecuritySystemState> getTargetSecuritySystemState();

    /**
     * Subscribes to changes in the pending, but not yet completed, state of the security system.
     *
     * @param callback
     *            the function to call when the state changes.
     */
    void subscribeTargetSecuritySystemState(CharacteristicCallback callback);

    /**
     * Unsubscribes from changes in the pending, but not yet completed, state of the security system.
     */
    void unsubscribeTargetSecuritySystemState();

    /**
     * Retrieves the alarm type of the security system.
     *
     * @return alarm type of the security system.
     */
    CompletableFuture<SecuritySystemAlarmType> getAlarmTypeState();

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new SecuritySystemService(this));
    }
}
