package com.automaton.characteristics;

import java.util.concurrent.CompletableFuture;

import com.automaton.accessories.TemperatureSensor;

public abstract class AbstractTemperature extends AbstractFloatCharacteristic implements EventableCharacteristic {
    public AbstractTemperature(String type, boolean isWritable, String description, TemperatureSensor sensor) {
        super(type, isWritable, true, description, sensor.getMinimumTemperature(), sensor.getMaximumTemperature(), 0.1D,
                "celsius");
    }

    public static class CoolingThresholdTemperature extends AbstractTemperature {
        private final TemperatureSensor.CoolingThermostat thermostat;

        public CoolingThresholdTemperature(TemperatureSensor.CoolingThermostat thermostat) {
            super("0000000D-0000-1000-8000-0026BB765291", true, "Temperature above which cooling will be active",
                    (TemperatureSensor) thermostat);
            this.thermostat = thermostat;
        }

        public void subscribe(CharacteristicCallback callback) {
            this.thermostat.subscribe(callback);
        }

        public void unsubscribe() {
            this.thermostat.unsubscribe();
        }

        protected CompletableFuture<Double> getDoubleValue() {
            return this.thermostat.getCoolingThresholdTemperature();
        }

        protected void setValue(Double value) throws Exception {
            this.thermostat.setCoolingThresholdTemperature(value);
        }
    }

    public static class Temperature extends AbstractTemperature {
        private final TemperatureSensor sensor;

        public Temperature(TemperatureSensor thermostat) {
            super("00000011-0000-1000-8000-0026BB765291", false, "Current Temperature", thermostat);
            this.sensor = thermostat;
        }

        public void subscribe(CharacteristicCallback callback) {
            this.sensor.subscribe(callback);
        }

        public void unsubscribe() {
            this.sensor.unsubscribe();
        }

        protected CompletableFuture<Double> getDoubleValue() {
            return this.sensor.getCurrentTemperature();
        }

        protected void setValue(Double value) throws Exception {
        }
    }
}
