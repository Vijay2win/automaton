package com.automaton.characteristics;

import java.util.concurrent.CompletableFuture;

import com.automaton.accessories.TemperatureSensor;

public abstract class AbstractHeatingCoolingMode extends AbstractEnumCharacteristic implements EventableCharacteristic {
    public AbstractHeatingCoolingMode(String type, boolean isWritable, String description) {
        super(type, isWritable, true, description, 3);
    }

    protected final void setValue(Integer value) throws Exception {
        setModeValue(TemperatureSensor.Thermostats.ThermostatMode.fromCode(value));
    }

    protected final CompletableFuture<Integer> getValue() {
        return getModeValue().thenApply(t -> Integer.valueOf(t.getCode()));
    }

    protected abstract void setModeValue(TemperatureSensor.Thermostats.ThermostatMode paramThermostatMode)
            throws Exception;

    protected abstract CompletableFuture<TemperatureSensor.Thermostats.ThermostatMode> getModeValue();

    public static class TargetHeatingCoolingMode extends AbstractHeatingCoolingMode {
        private final TemperatureSensor.Thermostats thermostat;

        public TargetHeatingCoolingMode(TemperatureSensor.Thermostats thermostat) {
            super("00000033-0000-1000-8000-0026BB765291", true, "Target Mode");
            this.thermostat = thermostat;
        }

        protected void setModeValue(TemperatureSensor.Thermostats.ThermostatMode mode) throws Exception {
            this.thermostat.setTargetMode(mode);
        }

        protected CompletableFuture<TemperatureSensor.Thermostats.ThermostatMode> getModeValue() {
            return this.thermostat.getTargetMode();
        }

        public void subscribe(CharacteristicCallback callback) {
            this.thermostat.subscribeTargetMode(callback);
        }

        public void unsubscribe() {
            this.thermostat.unsubscribeTargetMode();
        }
    }

    public static class ThermostatModeCharacteristic extends AbstractHeatingCoolingMode {
        private final TemperatureSensor.Thermostats thermostat;

        public ThermostatModeCharacteristic(TemperatureSensor.Thermostats thermostat) {
            super("0000000F-0000-1000-8000-0026BB765291", false, "Current Mode");
            this.thermostat = thermostat;
        }

        protected void setModeValue(TemperatureSensor.Thermostats.ThermostatMode mode) throws Exception {
        }

        protected CompletableFuture<TemperatureSensor.Thermostats.ThermostatMode> getModeValue() {
            return this.thermostat.getCurrentMode();
        }

        public void subscribe(CharacteristicCallback callback) {
            this.thermostat.subscribeCurrentMode(callback);
        }

        public void unsubscribe() {
            this.thermostat.unsubscribeCurrentMode();
        }
    }
}
