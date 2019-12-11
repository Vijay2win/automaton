
package com.automaton.myq;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.accessories.GarageDoor;
import com.automaton.characteristics.CharacteristicCallback;

class GarageDoorDevice implements GarageDoor {
    private static final Logger logger = LoggerFactory.getLogger(GarageDoorDevice.class);

    public final String serialNumber;
    public final String deviceFamily;
    public final String devicePlatform;
    public final String deviceType;
    public final String name;
    private final GarageDoorHub hub;

    public volatile DoorState state;

    private CharacteristicCallback targetDoorStateCallback;
    private CharacteristicCallback obstructionCallback;
    private CharacteristicCallback callback;

    private GarageDoorDevice(String serialNumber, String deviceFamily, String devicePlatform, String deviceType,
            String name, DoorState state, GarageDoorHub hub) {
        this.serialNumber = serialNumber;
        this.deviceFamily = deviceFamily;
        this.devicePlatform = devicePlatform;
        this.deviceType = deviceType;
        this.name = name;
        this.state = state;
        this.hub = hub;
    }

    @Override
    public int getId() {
        return 12493434;
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public void identify() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public String getModel() {
        return devicePlatform;
    }

    @Override
    public String getManufacturer() {
        return devicePlatform;
    }

    @Override
    public void subscribe(CharacteristicCallback callback) {
        this.callback = callback;
    }

    @Override
    public void unsubscribe() {
        this.callback = null;
    }

    @Override
    public CompletableFuture<DoorState> getCurrentDoorState() {
        this.state = hub.state(this);
        return CompletableFuture.completedFuture(state);
    }

    @Override
    public CompletableFuture<DoorState> getTargetDoorState() {
        return CompletableFuture.completedFuture(DoorState.CLOSED);
    }

    @Override
    public CompletableFuture<Boolean> getObstructionDetected() {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Void> setTargetDoorState(DoorState state) throws Exception {
        logger.info("Garage door state setting to: {} ", state);
        switch (state) {
        case OPEN:
            hub.open(this);
            break;
        case CLOSED:
            hub.close(this);
            break;
        default:
            break;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeTargetDoorState(CharacteristicCallback callback) {
        this.targetDoorStateCallback = callback;
    }

    @Override
    public void subscribeObstructionDetected(CharacteristicCallback callback) {
        this.obstructionCallback = callback;
    }

    @Override
    public void unsubscribeTargetDoorState() {
        targetDoorStateCallback = null;
    }

    @Override
    public void unsubscribeObstructionDetected() {
        obstructionCallback = null;
    }

    public static GarageDoorDevice parse(Map raw, GarageDoorHub hub) {
        String serialNumber = raw.get("serial_number").toString();
        String deviceFamily = raw.get("device_family").toString();
        String devicePlatform = raw.get("device_platform").toString();
        String name = raw.get("name").toString() + " Main";
        String deviceType = raw.get("device_type").toString();
        Object rawState = ((Map) raw.get("state")).get("door_state");
        DoorState state = DoorState.STOPPED;
        if (rawState != null) {
            logger.info("Got the door status to be {}", rawState);
            state = DoorState.valueOf(rawState.toString().toUpperCase());
        }
        return new GarageDoorDevice(serialNumber, deviceFamily, devicePlatform, deviceType, name, state, hub);
    }
}