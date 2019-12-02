package com.automaton.characteristics;

@FunctionalInterface
public interface CharacteristicCallback {
    void changed();

    default boolean isRemovable() {
        return true;
    }
}
