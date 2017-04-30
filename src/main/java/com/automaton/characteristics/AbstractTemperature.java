package com.automaton.characteristics;

import java.util.concurrent.CompletableFuture;

import com.automaton.accessories.TemperatureSensor;
import com.automaton.accessories.TemperatureSensor.CoolingThermostat;

public abstract class AbstractTemperature extends AbstractFloatCharacteristic implements EventableCharacteristic {
    public AbstractTemperature(String type, boolean isWritable, String description, TemperatureSensor sensor) {
        super(type, isWritable, true, description, sensor.getMinimumTemperature(), sensor.getMaximumTemperature(), 0.1, "celsius");
    }

    public static class CoolingThresholdTemperature extends AbstractTemperature {
        private final CoolingThermostat thermostat;

        public CoolingThresholdTemperature(CoolingThermostat thermostat) {
            super("0000000D-0000-1000-8000-0026BB765291", true, "Temperature above which cooling will be active", thermostat);
            this.thermostat = thermostat;
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            thermostat.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            thermostat.unsubscribe();
        }

        @Override
        protected CompletableFuture<Double> getDoubleValue() {
            return thermostat.getCoolingThresholdTemperature();
        }

        @Override
        protected void setValue(Double value) throws Exception {
            thermostat.setCoolingThresholdTemperature(value);
        }
    }

    public static class Temperature extends AbstractTemperature {
        private final TemperatureSensor sensor;

        public Temperature(TemperatureSensor thermostat) {
            super("00000011-0000-1000-8000-0026BB765291", false, "Current Temperature", thermostat);
            this.sensor = thermostat;
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
        protected CompletableFuture<Double> getDoubleValue() {
            return sensor.getCurrentTemperature();
        }

        @Override
        protected void setValue(Double value) throws Exception {
            // Not writable
        }
    }
}
