package com.automaton.characteristics;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.json.JsonNumber;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import com.automaton.accessories.Fan;
import com.automaton.accessories.Light.DimmableLight;
import com.automaton.accessories.WindowCovering;
import com.automaton.accessories.WindowCovering.HorizontalTiltingWindowCovering;
import com.automaton.accessories.WindowCovering.VerticalTiltingWindowCovering;

public abstract class AbstractIntegerCharacteristic extends AbstractCharacteristic<Integer> {
    private final int minValue, maxValue;
    private final String unit;

    public AbstractIntegerCharacteristic(String type, boolean isWritable, boolean isReadable, String description, int minValue, int maxValue, String unit) {
        super(type, "int", isWritable, isReadable, description);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
    }

    @Override
    protected CompletableFuture<JsonObjectBuilder> makeBuilder(int iid) {
        return super.makeBuilder(iid).thenApply(builder -> {
            return builder.add("minValue", minValue).add("maxValue", maxValue).add("minStep", 1).add("unit", unit);
        });
    }

    @Override
    protected Integer getDefault() {
        return minValue;
    }

    @Override
    protected Integer convert(JsonValue jsonValue) {
        return ((JsonNumber) jsonValue).intValue();
    }

    public static class Brightness extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final DimmableLight lightbulb;

        public Brightness(DimmableLight lightbulb) {
            super("00000008-0000-1000-8000-0026BB765291", true, true, "Adjust brightness of the light", 0, 100, "%");
            this.lightbulb = lightbulb;
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            lightbulb.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            lightbulb.unsubscribe();
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            lightbulb.setBrightness(value);
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return lightbulb.getBrightness();
        }
    }

    public static class BatteryLevel extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final Supplier<CompletableFuture<Integer>> getter;
        private final Consumer<CharacteristicCallback> subscriber;
        private final Runnable unsubscriber;

        public BatteryLevel(Supplier<CompletableFuture<Integer>> getter, Consumer<CharacteristicCallback> subscriber, Runnable unsubscriber) {
            super("00000068-0000-1000-8000-0026BB765291", false, true, "Battery Level", 0, 100, "%");
            this.getter = getter;
            this.subscriber = subscriber;
            this.unsubscriber = unsubscriber;
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return getter.get();
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Read Only
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            subscriber.accept(callback);
        }

        @Override
        public void unsubscribe() {
            unsubscriber.run();
        }
    }

    public static class FanSpeed extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final Fan fan;

        public FanSpeed(Fan fan) {
            super("00000029-0000-1000-8000-0026BB765291", true, true, "Rotation speed", 0, 100, "%");
            this.fan = fan;
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            fan.subscribeRotationSpeed(callback);
        }

        @Override
        public void unsubscribe() {
            fan.unsubscribeRotationSpeed();
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            fan.setRotationSpeed(value);
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return fan.getRotationSpeed();
        }
    }

    public static class HorizontalTiltAngle extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final HorizontalTiltingWindowCovering windowCovering;

        public HorizontalTiltAngle(HorizontalTiltingWindowCovering windowCovering) {
            super("0000006C-0000-1000-8000-0026BB765291", false, true, "The current horizontal tilt angle", -90, 90, "Arc Degree");
            this.windowCovering = windowCovering;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Read Only
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return windowCovering.getCurrentHorizontalTiltAngle();
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            windowCovering.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            windowCovering.unsubscribe();
        }
    }

    public static class Position extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final WindowCovering windowCovering;

        public Position(WindowCovering windowCovering) {
            super("0000006D-0000-1000-8000-0026BB765291", false, true, "The current position", 0, 100, "%");
            this.windowCovering = windowCovering;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Read Only
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return windowCovering.getCurrentPosition();
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            windowCovering.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            windowCovering.unsubscribe();
        }
    }

    public static class TargetHorizontalTiltAngle extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final HorizontalTiltingWindowCovering windowCovering;

        public TargetHorizontalTiltAngle(HorizontalTiltingWindowCovering windowCovering) {
            super("0000007B-0000-1000-8000-0026BB765291", true, true, "The target horizontal tilt angle", -90, 90, "Arc Degree");
            this.windowCovering = windowCovering;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            windowCovering.setTargetHorizontalTiltAngle(value);
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return windowCovering.getTargetHorizontalTiltAngle();
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            windowCovering.subscribeTargetHorizontalTiltAngle(callback);
        }

        @Override
        public void unsubscribe() {
            windowCovering.unsubscribeTargetHorizontalTiltAngle();
        }
    }

    public static class TargetPosition extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final WindowCovering windowCovering;

        public TargetPosition(WindowCovering windowCovering) {
            super("0000007C-0000-1000-8000-0026BB765291", true, true, "The target position", 0, 100, "%");
            this.windowCovering = windowCovering;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            windowCovering.setTargetPosition(value);
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return windowCovering.getTargetPosition();
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            windowCovering.subscribeTargetPosition(callback);
        }

        @Override
        public void unsubscribe() {
            windowCovering.unsubscribeTargetPosition();
        }
    }

    public static class VerticalTiltAngle extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final VerticalTiltingWindowCovering windowCovering;

        public VerticalTiltAngle(VerticalTiltingWindowCovering windowCovering) {
            super("0000006E-0000-1000-8000-0026BB765291", false, true, "The current vertical tilt angle", -90, 90, "Arc Degree");
            this.windowCovering = windowCovering;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Read Only
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return windowCovering.getCurrentVerticalTiltAngle();
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            windowCovering.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            windowCovering.unsubscribe();
        }
    }

    public static class VerticalTiltAngleTarget extends AbstractIntegerCharacteristic implements EventableCharacteristic {
        private final VerticalTiltingWindowCovering windowCovering;

        public VerticalTiltAngleTarget(VerticalTiltingWindowCovering windowCovering) {
            super("0000007D-0000-1000-8000-0026BB765291", true, true, "The target vertical tilt angle", -90, 90, "Arc Degree");
            this.windowCovering = windowCovering;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            windowCovering.setTargetVerticalTiltAngle(value);
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return windowCovering.getTargetVerticalTiltAngle();
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            windowCovering.subscribeTargetVerticalTiltAngle(callback);
        }

        @Override
        public void unsubscribe() {
            windowCovering.unsubscribeTargetVerticalTiltAngle();
        }
    }
}
