package com.automaton;

import java.util.*;

import com.automaton.accessories.Accessory;
import com.automaton.accessories.BatteryAccessory;
import com.automaton.characteristics.AbstractIntegerCharacteristic.BatteryLevel;
import com.automaton.characteristics.AbstractStaticCharacteristic.Name;
import com.automaton.characteristics.Characteristic;

public abstract class AbstractAccessoryService {
    private final String type;
    private final List<Characteristic> characteristics = new LinkedList<>();

    public AbstractAccessoryService(String type, Accessory accessory, String serviceName) {
        this.type = type;

        if (accessory != null) {
            addCharacteristic(new Name(serviceName));
            // If battery operated accessory then add BatteryLevelCharacteristic
            if (accessory instanceof BatteryAccessory) {
                BatteryAccessory batteryAccessory = (BatteryAccessory) accessory;
                addCharacteristic(new BatteryLevel(batteryAccessory::getBatteryLevelState, batteryAccessory::subscribe, batteryAccessory::unsubscribe));
            }
        }
    }

    public List<Characteristic> getCharacteristics() {
        return Collections.unmodifiableList(characteristics);
    }

    public String getType() {
        return type;
    }

    protected void addCharacteristic(Characteristic characteristic) {
        this.characteristics.add(characteristic);
    }
}
