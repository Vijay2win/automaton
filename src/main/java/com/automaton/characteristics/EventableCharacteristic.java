package com.automaton.characteristics;

/**
 * A characteristic that can be listened to by the connected iOS device.
 */
public interface EventableCharacteristic extends Characteristic {

    /**
     * Begin listening to changes to this characteristic. When a change is made, call the provided function.
     */
    void subscribe(CharacteristicCallback callback);

    /**
     * Stop listening to changes to this characteristic.
     */
    void unsubscribe();
}
