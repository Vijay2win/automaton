package com.automaton.characteristics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.accessories.*;

public abstract class AbstractCharacteristic<T> implements Characteristic {
    private final Logger logger = LoggerFactory.getLogger(AbstractCharacteristic.class);

    private final String type;
    private final String format;
    private final boolean isWritable;
    private final boolean isReadable;
    private final boolean isEventable;
    private final String description;

    public AbstractCharacteristic(String type, String format, boolean isWritable, boolean isReadable,
            String description) {
        if (type == null || format == null || description == null) {
            throw new NullPointerException();
        }
        this.type = type;
        this.format = format;
        this.isWritable = isWritable;
        this.isReadable = isReadable;
        this.isEventable = this instanceof EventableCharacteristic;
        this.description = description;
    }

    public final CompletableFuture<JsonObject> toJson(int iid) {
        return makeBuilder(iid).thenApply(builder -> builder.build());
    }

    protected CompletableFuture<JsonObjectBuilder> makeBuilder(int instanceId) {
        return getValue().exceptionally(t -> {
            this.logger.error("Could not retrieve value " + getClass().getName(), t);
            return null;
        }).thenApply(value -> {
            JsonArrayBuilder perms = Json.createArrayBuilder();
            if (this.isWritable) {
                perms.add("pw");
            }
            if (this.isReadable) {
                perms.add("pr");
            }
            if (this.isEventable) {
                perms.add("ev");
            }

            JsonObjectBuilder builder = Json.createObjectBuilder().add("iid", instanceId).add("type", this.type)
                    .add("perms", (JsonValue) perms.build()).add("format", this.format).add("events", false)
                    .add("bonjour", false).add("description", this.description);
            setJsonValue(builder, (T) value);
            return builder;
        });
    }

    public final void setValue(JsonValue jsonValue) {
        try {
            setValue(convert(jsonValue));
        } catch (Exception e) {
            this.logger.error("Error while setting JSON value", e);
        }
    }

    public void supplyValue(JsonObjectBuilder builder) {
        try {
            setJsonValue(builder, getValue().get());
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            this.logger.error("Error retrieving value", e);
            setJsonValue(builder, getDefault());
        }
    }

    protected abstract T convert(JsonValue paramJsonValue);

    protected abstract void setValue(T paramT) throws Exception;

    protected void setJsonValue(JsonObjectBuilder builder, T value) {
        if (value instanceof Boolean) {
            builder.add("value", (Boolean) value);
        } else if (value instanceof Double) {
            builder.add("value", (Double) value);
        } else if (value instanceof Integer) {
            builder.add("value", (Integer) value);
        } else if (value instanceof Long) {
            builder.add("value", (Long) value);
        } else if (value instanceof BigInteger) {
            builder.add("value", (BigInteger) value);
        } else if (value instanceof BigDecimal) {
            builder.add("value", (BigDecimal) value);
        } else if (value != null) {
            builder.add("value", value.toString());
        }
        // Do not add null value, HomeKit cannot handle that
    }

    protected abstract CompletableFuture<T> getValue();

    protected abstract T getDefault();

    public static abstract class BooleanCharacteristic extends AbstractCharacteristic<Boolean> {
        public BooleanCharacteristic(String type, boolean isWritable, boolean isReadable, String description) {
            super(type, "bool", isWritable, isReadable, description);
        }

        protected Boolean convert(JsonValue jsonValue) {
            if (jsonValue.getValueType().equals(JsonValue.ValueType.NUMBER))
                return ((JsonNumber) jsonValue).intValue() > 0;
            return jsonValue.equals(JsonValue.TRUE);
        }

        protected Boolean getDefault() {
            return false;
        }
    }

    public static abstract class WriteOnlyBooleanCharacteristic extends BooleanCharacteristic {
        public WriteOnlyBooleanCharacteristic(String type, String description) {
            super(type, true, false, description);
        }

        @Override
        protected final CompletableFuture<Boolean> getValue() {
            return CompletableFuture.completedFuture(false);
        }

        @Override
        protected final void setJsonValue(JsonObjectBuilder builder, Boolean value) {
            // Do nothing - non-readable characteristics cannot have a value key set
        }
    }

    public static class Identify extends WriteOnlyBooleanCharacteristic {
        private Accessory accessory;

        public Identify(Accessory accessory) throws Exception {
            super("00000014-0000-1000-8000-0026BB765291",
                    "Identifies the accessory via a physical action on the accessory");
            this.accessory = accessory;
        }

        public void setValue(Boolean value) throws Exception {
            if (value)
                this.accessory.identify();
        }
    }

    public static class HoldPosition extends BooleanCharacteristic {
        private final WindowCovering windowCovering;

        public HoldPosition(WindowCovering windowCovering) {
            super("0000006F-0000-1000-8000-0026BB765291", true, false, "Whether or not to hold position");
            this.windowCovering = windowCovering;
        }

        protected void setValue(Boolean value) throws Exception {
            this.windowCovering.setHoldPosition(value);
        }

        protected CompletableFuture<Boolean> getValue() {
            // Write only
            return CompletableFuture.completedFuture(null);
        }
    }

    public static class MotionDetectedState extends BooleanCharacteristic implements EventableCharacteristic {
        private final MotionSensor motionSensor;

        public MotionDetectedState(MotionSensor motionSensor) {
            super("00000022-0000-1000-8000-0026BB765291", false, true, "Motion Detected");
            this.motionSensor = motionSensor;
        }

        @Override
        protected CompletableFuture<Boolean> getValue() {
            return motionSensor.getMotionDetected();
        }

        @Override
        protected void setValue(Boolean value) throws Exception {}

        @Override
        public void subscribe(CharacteristicCallback callback) {
            motionSensor.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            motionSensor.unsubscribe();
        }
    }

    public static class ObstructionDetected extends BooleanCharacteristic implements EventableCharacteristic {
        private final Supplier<CompletableFuture<Boolean>> getter;
        private final Consumer<CharacteristicCallback> subscriber;
        private final Runnable unsubscriber;

        public ObstructionDetected(Supplier<CompletableFuture<Boolean>> getter,
                Consumer<CharacteristicCallback> subscriber, Runnable unsubscriber) {
            super("00000024-0000-1000-8000-0026BB765291", false, true, "An obstruction has been detected");
            this.getter = getter;
            this.subscriber = subscriber;
            this.unsubscriber = unsubscriber;
        }

        @Override
        protected void setValue(Boolean value) throws Exception {
        }

        @Override
        protected CompletableFuture<Boolean> getValue() {
            return getter.get();
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

    public static class OutletInUse extends BooleanCharacteristic implements EventableCharacteristic {
        private final Outlet outlet;

        public OutletInUse(Outlet outlet) {
            super("00000026-0000-1000-8000-0026BB765291", false, true, "The outlet is in use");
            this.outlet = outlet;
        }

        @Override
        protected void setValue(Boolean value) throws Exception {
            // Read Only
        }

        @Override
        protected CompletableFuture<Boolean> getValue() {
            return outlet.getOutletInUse();
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            outlet.subscribeOutletInUse(callback);
        }

        @Override
        public void unsubscribe() {
            outlet.unsubscribe();
        }
    }
}
