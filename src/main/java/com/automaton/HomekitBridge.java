package com.automaton;

import java.util.Collection;
import java.util.Collections;

import com.automaton.accessories.Accessory;
import com.automaton.characteristics.CharacteristicCallback;

public class HomekitBridge implements Accessory {
    private final String label;
    private final String serialNumber;
    private final String model;
    private final String manufacturer;

    public HomekitBridge(String label, String serialNumber, String model, String manufacturer) {
        this.label = label;
        this.serialNumber = serialNumber;
        this.model = model;
        this.manufacturer = manufacturer;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public String getManufacturer() {
        return manufacturer;
    }

    @Override
    public Collection<AbstractAccessoryService> getServices() {
        return Collections.emptyList();
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public void identify() {
        // do nothing.
    }

    @Override
    public void subscribe(CharacteristicCallback callback) {
    }

    @Override
    public void unsubscribe() {
    }
}
