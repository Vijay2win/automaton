package com.automaton.accessories;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.TemperatureSensorService;
import com.automaton.AccessoryServices.ThermostatService;
import com.automaton.characteristics.CharacteristicCallback;

/**
 * A temperature sensor that reports the current temperature
 *
 * @author Andy Lintner
 */
public interface TemperatureSensor extends Accessory {
    public enum TemperatureUnitEnum {
        CELSIUS(0), FAHRENHEIT(1);

        private final int code;

        private TemperatureUnitEnum(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        static TemperatureUnitEnum fromCode(Integer code) {
            return values()[code];
        }
    }

    /**
     * Retrieves the current temperature, in celsius degrees.
     * 
     * @return a future that will contain the temperature.
     */
    CompletableFuture<Double> getCurrentTemperature();

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new TemperatureSensorService(this));
    }

    /**
     * Retrieves the minimum temperature, in celsius degrees, the thermostat can be set to.
     * 
     * @return the minimum temperature.
     */
    double getMinimumTemperature();

    /**
     * Retrieves the maximum temperature, in celsius degrees, the thermostat can be set to.
     * 
     * @return the maximum temperature.
     */
    double getMaximumTemperature();

    /**
     * Retrieves the temperature unit of the thermostat. The impact of this is unclear, as the actual temperature is
     * always communicated in celsius degrees, and the iOS device uses the user's locale to determine the unit to
     * convert to.
     * 
     * @return the temperature unit of the thermostat.
     */
    default TemperatureUnitEnum getTemperatureUnit() {
        return TemperatureUnitEnum.CELSIUS;
    }

    public interface Thermostats extends TemperatureSensor {
        public enum ThermostatMode {
            OFF(0), HEAT(1), COOL(2), AUTO(3);

            private final static Map<Integer, ThermostatMode> reverse = Arrays.stream(ThermostatMode.values()).collect(Collectors.toMap(t -> t.getCode(), t -> t));

            public static ThermostatMode fromCode(Integer code) {
                return reverse.get(code);
            }

            private final int code;

            private ThermostatMode(int code) {
                this.code = code;
            }

            public int getCode() {
                return code;
            }
        }

        /**
         * Retrieves the current {@link ThermostatMode} of the thermostat.
         */
        CompletableFuture<ThermostatMode> getCurrentMode();

        /**
         * Subscribes to changes in the {@link ThermostatMode} of the thermostat.
         */
        void subscribeCurrentMode(CharacteristicCallback callback);

        /**
         * Unsubscribes from changes in the mode of the thermostat.
         */
        void unsubscribeCurrentMode();

        /**
         * Sets the {@link ThermostatMode} of the thermostat.
         */
        void setTargetMode(ThermostatMode mode) throws Exception;

        /**
         * Retrieves the pending, but not yet complete, {@link ThermostatMode} of the thermostat.
         */
        CompletableFuture<ThermostatMode> getTargetMode();

        /**
         * Subscribes to changes in the pending, but not yet complete, {@link ThermostatMode} of the thermostat.
         */
        void subscribeTargetMode(CharacteristicCallback callback);

        /**
         * Unsubscribes from changes in the pending, but not yet complete, {@link ThermostatMode} of the thermostat.
         */
        void unsubscribeTargetMode();

        /**
         * Retrieves the target temperature, in celsius degrees.
         */
        CompletableFuture<Double> getTargetTemperature();

        /**
         * Sets the target temperature.
         */
        void setTargetTemperature(Double value) throws Exception;

        @Override
        default Collection<AbstractAccessoryService> getServices() {
            return Collections.singleton(new ThermostatService(this));
        }
    }

    public interface CoolingThermostat extends Thermostats {
        /**
         * Retrieves the temperature above which the thermostat should begin cooling.
         * 
         * @return a future that will contain the threshold temperature, in celsius degrees.
         */
        CompletableFuture<Double> getCoolingThresholdTemperature();

        /**
         * Sets the temperature above which the thermostat should begin cooling.
         * 
         * @param value
         *            the threshold temperature, in celsius degrees.
         * @throws Exception
         *             when the threshold temperature cannot be changed.
         */
        void setCoolingThresholdTemperature(Double value) throws Exception;
    }

    public interface HeatingThermostat extends Thermostats {
        /**
         * Retrieves the temperature below which the thermostat should begin heating.
         * 
         * @return a future that will contain the threshold temperature, in celsius degrees.
         */
        CompletableFuture<Double> getHeatingThresholdTemperature();

        /**
         * Sets the temperature below which the thermostat should begin heating.
         * 
         * @param value
         *            the threshold temperature, in celsius degrees.
         * @throws Exception
         *             when the threshold temperature cannot be changed.
         */
        void setHeatingThresholdTemperature(Double value) throws Exception;
    }
}
