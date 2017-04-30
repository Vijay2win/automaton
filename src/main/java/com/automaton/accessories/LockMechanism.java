package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.LockMechanismService;

/**
 * <p>
 * A lock capable of exposing its binary locked state. For a lock that can be locked/unlocked, use
 * {@link LockableLockMechanism}.
 * </p>
 *
 * <p>
 * Locks that run on batteries will need to implement this interface and also implement {@link BatteryAccessory}.
 * </p>
 *
 * @author Andy Lintner
 */
public interface LockMechanism extends Accessory {
    public enum LockMechanismState {
        UNSECURED(0), SECURED(1), JAMMED(2), UNKNOWN(3);

        private final int code;

        private LockMechanismState(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static LockMechanismState fromCode(Integer code) {
            return values()[code];
        }
    }

    /**
     * Retrieves the current binary state of the lock.
     * 
     * @return a future that will contain the binary state.
     */
    CompletableFuture<LockMechanismState> getState();

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new LockMechanismService(this));
    }

    public interface LockableLockMechanism extends LockMechanism {
        /**
         * Sets the binary state of the lock mechanism.
         * 
         * @param state
         *            true for a locked mechanism, false for unlocked.
         * @throws Exception
         *             when the change cannot be made.
         */
        void setTargetMechanismState(LockMechanismState state) throws Exception;

        /**
         * Retrieves the pending, but not yet completed, state of the lock mechanism.
         * 
         * @return the binary state
         */
        CompletableFuture<LockMechanismState> getTargetMechanismState();
    }
}
