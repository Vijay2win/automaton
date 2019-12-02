package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.ContactSensorService;

/**
 * <p>
 * A contact sensor that reports whether contact is detected or not. Typical contact sensors are window/door sensors.
 * When contact is detected it means that the door/window is closed.
 * </p>
 *
 * <p>
 * Contact sensors that run on batteries will need to implement this interface and also implement
 * {@link BatteryAccessory}.
 * </p>
 *
 * @author Gaston Dombiak
 */
public interface ContactSensor extends Accessory {
    public enum ContactState {
        DETECTED(0), NOT_DETECTED(1);

        private final int code;

        ContactState(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    /**
     * Retrieves the state of the contact. This is whether the contact is detected or not. Detected contact means
     * door/window is closed.
     *
     * @return a future that will contain the contact's state
     */
    CompletableFuture<ContactState> getCurrentState();

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new ContactSensorService(this));
    }
}
