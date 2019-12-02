package com.automaton.characteristics;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.json.*;

import com.automaton.accessories.*;

public abstract class AbstractIntegerCharacteristic extends AbstractCharacteristic<Integer> {
    private final int minValue;
    private final int maxValue;
    private final String unit;

    public AbstractIntegerCharacteristic(String type, boolean isWritable, boolean isReadable, String description,
            int minValue, int maxValue, String unit) {
        super(type, "int", isWritable, isReadable, description);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
    }

    protected CompletableFuture<JsonObjectBuilder> makeBuilder(int iid) {
        return super.makeBuilder(iid).thenApply(builder -> builder.add("minValue", this.minValue)
                .add("maxValue", this.maxValue).add("minStep", 1).add("unit", this.unit));
    }

    protected Integer getDefault() {
        return Integer.valueOf(this.minValue);
    }

    protected Integer convert(JsonValue jsonValue) {
        return Integer.valueOf(((JsonNumber) jsonValue).intValue());
    }

    public static class Brightness extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final Light.DimmableLight lightbulb;

        public Brightness(Light.DimmableLight lightbulb) {
            super("00000008-0000-1000-8000-0026BB765291", true, true, "Adjust brightness of the light", 0, 100, "%");
            this.lightbulb = lightbulb;
        }

        public void subscribe(CharacteristicCallback callback) {
            this.lightbulb.subscribe(callback);
        }

        public void unsubscribe() {
            this.lightbulb.unsubscribe();
        }

        protected void setValue(Integer value) throws Exception {
            this.lightbulb.setBrightness(value);
        }

        protected CompletableFuture<Integer> getValue() {
            return this.lightbulb.getBrightness();
        }
    }

    public static class BatteryLevel extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final Supplier<CompletableFuture<Integer>> getter;
        private final Consumer<CharacteristicCallback> subscriber;
        private final Runnable unsubscriber;

        public BatteryLevel(Supplier<CompletableFuture<Integer>> getter, Consumer<CharacteristicCallback> subscriber,
                Runnable unsubscriber) {
            super("00000068-0000-1000-8000-0026BB765291", false, true, "Battery Level", 0, 100, "%");
            this.getter = getter;
            this.subscriber = subscriber;
            this.unsubscriber = unsubscriber;
        }

        protected CompletableFuture<Integer> getValue() {
            return this.getter.get();
        }

        protected void setValue(Integer value) throws Exception {
        }

        public void subscribe(CharacteristicCallback callback) {
            this.subscriber.accept(callback);
        }

        public void unsubscribe() {
            this.unsubscriber.run();
        }
    }

    public static class FanSpeed extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final Fan fan;

        public FanSpeed(Fan fan) {
            super("00000029-0000-1000-8000-0026BB765291", true, true, "Rotation speed", 0, 100, "%");
            this.fan = fan;
        }

        public void subscribe(CharacteristicCallback callback) {
            this.fan.subscribeRotationSpeed(callback);
        }

        public void unsubscribe() {
            this.fan.unsubscribeRotationSpeed();
        }

        protected void setValue(Integer value) throws Exception {
            this.fan.setRotationSpeed(value);
        }

        protected CompletableFuture<Integer> getValue() {
            return this.fan.getRotationSpeed();
        }
    }

    public static class HorizontalTiltAngle extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final WindowCovering.HorizontalTiltingWindowCovering windowCovering;

        public HorizontalTiltAngle(WindowCovering.HorizontalTiltingWindowCovering windowCovering) {
            super("0000006C-0000-1000-8000-0026BB765291", false, true, "The current horizontal tilt angle", -90, 90,
                    "Arc Degree");
            this.windowCovering = windowCovering;
        }

        protected void setValue(Integer value) throws Exception {
        }

        protected CompletableFuture<Integer> getValue() {
            return this.windowCovering.getCurrentHorizontalTiltAngle();
        }

        public void subscribe(CharacteristicCallback callback) {
            this.windowCovering.subscribe(callback);
        }

        public void unsubscribe() {
            this.windowCovering.unsubscribe();
        }
    }

    public static class Position extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final WindowCovering windowCovering;

        public Position(WindowCovering windowCovering) {
            super("0000006D-0000-1000-8000-0026BB765291", false, true, "The current position", 0, 100, "%");
            this.windowCovering = windowCovering;
        }

        protected void setValue(Integer value) throws Exception {
        }

        protected CompletableFuture<Integer> getValue() {
            return this.windowCovering.getCurrentPosition();
        }

        public void subscribe(CharacteristicCallback callback) {
            this.windowCovering.subscribe(callback);
        }

        public void unsubscribe() {
            this.windowCovering.unsubscribe();
        }
    }

    public static class TargetHorizontalTiltAngle extends AbstractIntegerCharacteristic
            implements EventableCharacteristic {
        private final WindowCovering.HorizontalTiltingWindowCovering windowCovering;

        public TargetHorizontalTiltAngle(WindowCovering.HorizontalTiltingWindowCovering windowCovering) {
            super("0000007B-0000-1000-8000-0026BB765291", true, true, "The target horizontal tilt angle", -90, 90,
                    "Arc Degree");
            this.windowCovering = windowCovering;
        }

        protected void setValue(Integer value) throws Exception {
            this.windowCovering.setTargetHorizontalTiltAngle(value.intValue());
        }

        protected CompletableFuture<Integer> getValue() {
            return this.windowCovering.getTargetHorizontalTiltAngle();
        }

        public void subscribe(CharacteristicCallback callback) {
            this.windowCovering.subscribeTargetHorizontalTiltAngle(callback);
        }

        public void unsubscribe() {
            this.windowCovering.unsubscribeTargetHorizontalTiltAngle();
        }
    }

    public static class TargetPosition extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final WindowCovering windowCovering;

        public TargetPosition(WindowCovering windowCovering) {
            super("0000007C-0000-1000-8000-0026BB765291", true, true, "The target position", 0, 100, "%");
            this.windowCovering = windowCovering;
        }

        protected void setValue(Integer value) throws Exception {
            this.windowCovering.setTargetPosition(value.intValue());
        }

        protected CompletableFuture<Integer> getValue() {
            return this.windowCovering.getTargetPosition();
        }

        public void subscribe(CharacteristicCallback callback) {
            this.windowCovering.subscribeTargetPosition(callback);
        }

        public void unsubscribe() {
            this.windowCovering.unsubscribeTargetPosition();
        }
    }

    public static class VerticalTiltAngle extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final WindowCovering.VerticalTiltingWindowCovering windowCovering;

        public VerticalTiltAngle(WindowCovering.VerticalTiltingWindowCovering windowCovering) {
            super("0000006E-0000-1000-8000-0026BB765291", false, true, "The current vertical tilt angle", -90, 90,
                    "Arc Degree");
            this.windowCovering = windowCovering;
        }

        protected void setValue(Integer value) throws Exception {
        }

        protected CompletableFuture<Integer> getValue() {
            return this.windowCovering.getCurrentVerticalTiltAngle();
        }

        public void subscribe(CharacteristicCallback callback) {
            this.windowCovering.subscribe(callback);
        }

        public void unsubscribe() {
            this.windowCovering.unsubscribe();
        }
    }

    public static class VerticalTiltAngleTarget extends AbstractIntegerCharacteristic
            implements EventableCharacteristic {
        private final WindowCovering.VerticalTiltingWindowCovering windowCovering;

        public VerticalTiltAngleTarget(WindowCovering.VerticalTiltingWindowCovering windowCovering) {
            super("0000007D-0000-1000-8000-0026BB765291", true, true, "The target vertical tilt angle", -90, 90,
                    "Arc Degree");
            this.windowCovering = windowCovering;
        }

        protected void setValue(Integer value) throws Exception {
            this.windowCovering.setTargetVerticalTiltAngle(value.intValue());
        }

        protected CompletableFuture<Integer> getValue() {
            return this.windowCovering.getTargetVerticalTiltAngle();
        }

        public void subscribe(CharacteristicCallback callback) {
            this.windowCovering.subscribeTargetVerticalTiltAngle(callback);
        }

        public void unsubscribe() {
            this.windowCovering.unsubscribeTargetVerticalTiltAngle();
        }
    }
}
