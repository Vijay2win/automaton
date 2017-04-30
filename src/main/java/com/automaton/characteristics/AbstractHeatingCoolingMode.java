package com.automaton.characteristics;

import java.util.concurrent.CompletableFuture;

import com.automaton.accessories.TemperatureSensor.Thermostats;
import com.automaton.accessories.TemperatureSensor.Thermostats.ThermostatMode;

public abstract class AbstractHeatingCoolingMode extends AbstractEnumCharacteristic implements EventableCharacteristic {
    public AbstractHeatingCoolingMode(String type, boolean isWritable, String description) {
        super(type, isWritable, true, description, 3);
    }

    protected final void setValue(Integer value) throws Exception {
        setModeValue(ThermostatMode.fromCode(value));
    }

    protected final CompletableFuture<Integer> getValue() {
        return getModeValue().thenApply(t -> t.getCode());
    }

    protected abstract void setModeValue(ThermostatMode mode) throws Exception;

    protected abstract CompletableFuture<ThermostatMode> getModeValue();

    public static class TargetHeatingCoolingMode extends AbstractHeatingCoolingMode {
        private final Thermostats thermostat;

        public TargetHeatingCoolingMode(Thermostats thermostat) {
            super("00000033-0000-1000-8000-0026BB765291", true, "Target Mode");
            this.thermostat = thermostat;
        }

        @Override
        protected void setModeValue(ThermostatMode mode) throws Exception {
            thermostat.setTargetMode(mode);
        }

        @Override
        protected CompletableFuture<ThermostatMode> getModeValue() {
            return thermostat.getTargetMode();
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            thermostat.subscribeTargetMode(callback);
        }

        @Override
        public void unsubscribe() {
            thermostat.unsubscribeTargetMode();
        }
    }

    public static class ThermostatModeCharacteristic extends AbstractHeatingCoolingMode {
        private final Thermostats thermostat;

        public ThermostatModeCharacteristic(Thermostats thermostat) {
            super("0000000F-0000-1000-8000-0026BB765291", false, "Current Mode");
            this.thermostat = thermostat;
        }

        @Override
        protected void setModeValue(ThermostatMode mode) throws Exception {
            // Not writable
        }

        @Override
        protected CompletableFuture<ThermostatMode> getModeValue() {
            return thermostat.getCurrentMode();
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            thermostat.subscribeCurrentMode(callback);
        }

        @Override
        public void unsubscribe() {
            thermostat.unsubscribeCurrentMode();
        }
    }
}
