package com.automaton.characteristics;

import java.util.concurrent.CompletableFuture;

import javax.json.*;

import com.automaton.accessories.*;
import com.automaton.accessories.CarbonMonoxideSensor.CarbonMonoxideDetectedState;
import com.automaton.accessories.ContactSensor.ContactState;
import com.automaton.accessories.Fan.RotationDirection;
import com.automaton.accessories.GarageDoor.DoorState;
import com.automaton.accessories.SecuritySystem.SecuritySystemAlarmType;

public abstract class AbstractEnumCharacteristic extends AbstractCharacteristic<Integer> {
    private final int maxValue;

    public AbstractEnumCharacteristic(String type, boolean isWritable, boolean isReadable, String description,
            int maxValue) {
        super(type, "int", isWritable, isReadable, description);
        this.maxValue = maxValue;
    }

    @Override
    protected CompletableFuture<JsonObjectBuilder> makeBuilder(int iid) {
        return super.makeBuilder(iid)
                .thenApply(builder -> builder.add("minValue", 0).add("maxValue", maxValue).add("minStep", 1));
    }

    @Override
    protected Integer convert(JsonValue jsonValue) {
        if (jsonValue instanceof JsonNumber) {
            return ((JsonNumber) jsonValue).intValue();
        } else if (jsonValue == JsonObject.TRUE) {
            return 1; // For at least one enum type (locks), homekit will send a true instead of 1
        } else if (jsonValue == JsonObject.FALSE) {
            return 0;
        } else {
            throw new IndexOutOfBoundsException("Cannot convert " + jsonValue.getClass() + " to int");
        }
    }

    @Override
    protected Integer getDefault() {
        return 0;
    }

    public static class SecuritySystemAlarmTypeCharacteristic extends AbstractEnumCharacteristic
            implements EventableCharacteristic {
        private final SecuritySystem securitySystem;

        public SecuritySystemAlarmTypeCharacteristic(SecuritySystem securitySystem) {
            super("0000008E-0000-1000-8000-0026BB765291", false, true, "Security system alarm type", 1);
            this.securitySystem = securitySystem;
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return securitySystem.getAlarmTypeState().thenApply(SecuritySystemAlarmType::getCode);
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Not writable
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            securitySystem.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            securitySystem.unsubscribe();
        }
    }

    public static class CarbonMonoxideDetected extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final CarbonMonoxideSensor carbonMonoxideSensor;

        public CarbonMonoxideDetected(CarbonMonoxideSensor carbonMonoxideSensor) {
            super("00000069-0000-1000-8000-0026BB765291", false, true, "Carbon Monoxide Detected", 1);
            this.carbonMonoxideSensor = carbonMonoxideSensor;
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return carbonMonoxideSensor.getCarbonMonoxideDetectedState()
                    .thenApply(CarbonMonoxideDetectedState::getCode);
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Read Only
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            carbonMonoxideSensor.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            carbonMonoxideSensor.unsubscribe();
        }
    }

    public static class ContactSensorState extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final ContactSensor contactSensor;

        public ContactSensorState(ContactSensor contactSensor) {
            super("0000006A-0000-1000-8000-0026BB765291", false, true, "Contact State", 1);
            this.contactSensor = contactSensor;
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return contactSensor.getCurrentState().thenApply(ContactState::getCode);
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Read Only
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            contactSensor.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            contactSensor.unsubscribe();
        }
    }

    public static class FanDirection extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final Fan fan;

        public FanDirection(Fan fan) {
            super("00000028-0000-1000-8000-0026BB765291", true, true, "Rotation Direction", 1);
            this.fan = fan;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            fan.setRotationDirection(RotationDirection.fromCode(value));
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return fan.getRotationDirection().thenApply(s -> s.getCode());
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            fan.subscribeRotationDirection(callback);
        }

        @Override
        public void unsubscribe() {
            fan.unsubscribeRotationDirection();
        }
    }

    public static class GarageDoorState extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final GarageDoor door;

        public GarageDoorState(GarageDoor door) {
            super("00000032-0000-1000-8000-0026BB765291", true, true, "Target Door State", 1);
            this.door = door;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            door.setTargetDoorState(DoorState.fromCode(value));
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return door.getTargetDoorState().thenApply(s -> s.getCode());
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            door.subscribeTargetDoorState(callback);
        }

        @Override
        public void unsubscribe() {
            door.unsubscribeTargetDoorState();
        }
    }

    public static class GarageTargetDoorState extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final GarageDoor door;

        public GarageTargetDoorState(GarageDoor door) {
            super("0000000E-0000-1000-8000-0026BB765291", false, true, "Current Door State", 4);
            this.door = door;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Read Only
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return door.getCurrentDoorState().thenApply(s -> s.getCode());
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            door.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            door.unsubscribe();
        }
    }
}
