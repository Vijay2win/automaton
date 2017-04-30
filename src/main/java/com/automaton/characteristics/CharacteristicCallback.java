package com.automaton.characteristics;

/**
 * A callback interface for notifying subscribers that a characteristic value has changed.
 * 
 * {@link EventableCharacteristic}s can be subscribed to, and in doing so, are supplied an instance of this class.
 * Implementors should call the {@link #changed()} method on the passed object when a subscribed characteristic changes.
 *
 * @author Andy Lintner
 */
@FunctionalInterface
public interface CharacteristicCallback {

    /**
     * Call when the value of the characteristic that was subscribed to when this object was passed changes.
     */
    void changed();

    default boolean isRemovable() {
        return true;
    }
}
