package com.automaton.characteristics;

public interface EventableCharacteristic extends Characteristic {
    void subscribe(CharacteristicCallback paramCharacteristicCallback);

    void unsubscribe();
}
