package com.automaton.characteristics;

import java.util.concurrent.CompletableFuture;

import javax.json.JsonNumber;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.accessories.HumiditySensor;
import com.automaton.accessories.Light.ColorfulLight;
import com.automaton.accessories.LightSensor;

public abstract class AbstractFloatCharacteristic extends AbstractCharacteristic<Double> {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractFloatCharacteristic.class);

    private final double minValue;
    private final double maxValue;
    private final double minStep;
    private final String unit;

    public AbstractFloatCharacteristic(String type, boolean isWritable, boolean isReadable, String description, double minValue, double maxValue, double minStep, String unit) {
        super(type, "float", isWritable, isReadable, description);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minStep = minStep;
        this.unit = unit;
    }

    @Override
    protected CompletableFuture<JsonObjectBuilder> makeBuilder(int iid) {
        return super.makeBuilder(iid).thenApply(builder -> builder.add("minValue", minValue).add("maxValue", maxValue).add("minStep", minStep).add("unit", unit));
    }

    @Override
    protected Double convert(JsonValue jsonValue) {
        return ((JsonNumber) jsonValue).doubleValue();
    }

    @Override
    protected final CompletableFuture<Double> getValue() {
        double rounder = 1 / this.minStep;
        return getDoubleValue().thenApply(d -> d == null ? null : Math.round(d * rounder) / rounder).thenApply(d -> {
            if (d != null) {
                if (d < minValue) {
                    LOGGER.warn("Detected value out of range " + d + ". Returning min value instead. Characteristic " + this);
                    return minValue;
                }
                if (d > maxValue) {
                    LOGGER.warn("Detected value out of range " + d + ". Returning max value instead. Characteristic " + this);
                    return maxValue;
                }
                return d;
            }
            return null;
        });
    }

    @Override
    protected Double getDefault() {
        return minValue;
    }

    /**
     * Supplies the value of this characteristic as a double.
     * 
     * @return a future that will contain the value.
     */
    protected abstract CompletableFuture<Double> getDoubleValue();

    public static class AmbientLightLevelCharacteristic extends AbstractFloatCharacteristic implements EventableCharacteristic {
        private final LightSensor lightSensor;

        public AmbientLightLevelCharacteristic(LightSensor lightSensor) {
            super("0000006B-0000-1000-8000-0026BB765291", false, true, "Current ambient light level", 0.0001, 100000, 0.0001, "lux");
            this.lightSensor = lightSensor;
        }

        @Override
        protected void setValue(Double value) throws Exception {
            // Read Only
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            lightSensor.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            lightSensor.unsubscribe();
        }

        @Override
        protected CompletableFuture<Double> getDoubleValue() {
            return lightSensor.getCurrentAmbientLightLevel();
        }
    }

    public static class Hue extends AbstractFloatCharacteristic implements EventableCharacteristic {
        private final ColorfulLight lightbulb;

        public Hue(ColorfulLight lightbulb) {
            super("00000013-0000-1000-8000-0026BB765291", true, true, "Adjust hue of the light", 0, 360, 1, "arcdegrees");
            this.lightbulb = lightbulb;
        }

        @Override
        protected void setValue(Double value) throws Exception {
            lightbulb.setHue(value);
        }

        @Override
        protected CompletableFuture<Double> getDoubleValue() {
            return lightbulb.getHue();
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            lightbulb.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            lightbulb.unsubscribe();
        }
    }

    public static class Humidity extends AbstractFloatCharacteristic implements EventableCharacteristic {
        private final HumiditySensor sensor;

        public Humidity(HumiditySensor sensor) {
            super("00000010-0000-1000-8000-0026BB765291", false, true, "Current relative humidity", 0, 100, 0.1, "%");
            this.sensor = sensor;
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            sensor.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            sensor.unsubscribe();
        }

        @Override
        protected void setValue(Double value) throws Exception {
            // Read Only
        }

        @Override
        protected CompletableFuture<Double> getDoubleValue() {
            return sensor.getCurrentRelativeHumidity();
        }
    }

    public static class Saturation extends AbstractFloatCharacteristic implements EventableCharacteristic {
        private final ColorfulLight lightbulb;

        public Saturation(ColorfulLight lightbulb) {
            super("0000002F-0000-1000-8000-0026BB765291", true, true, "Adjust saturation of the light", 0, 100, 1, "%");
            this.lightbulb = lightbulb;
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            lightbulb.subscribeSaturation(callback);
        }

        @Override
        public void unsubscribe() {
            lightbulb.unsubscribeSaturation();
        }

        @Override
        protected void setValue(Double value) throws Exception {
            lightbulb.setSaturation(value);
        }

        @Override
        protected CompletableFuture<Double> getDoubleValue() {
            return lightbulb.getSaturation();
        }
    }
}
