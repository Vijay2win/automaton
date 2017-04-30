package com.automaton;

import java.util.concurrent.CompletableFuture;

import com.automaton.accessories.*;
import com.automaton.accessories.Light.ColorfulLight;
import com.automaton.accessories.Light.DimmableLight;
import com.automaton.accessories.LockMechanism.LockableLockMechanism;
import com.automaton.accessories.TemperatureSensor.CoolingThermostat;
import com.automaton.accessories.TemperatureSensor.HeatingThermostat;
import com.automaton.accessories.TemperatureSensor.Thermostats;
import com.automaton.accessories.WindowCovering.HorizontalTiltingWindowCovering;
import com.automaton.accessories.WindowCovering.VerticalTiltingWindowCovering;
import com.automaton.characteristics.AbstractCharacteristic.*;
import com.automaton.characteristics.AbstractEnumCharacteristic.*;
import com.automaton.characteristics.AbstractFloatCharacteristic.*;
import com.automaton.characteristics.AbstractHeatingCoolingMode.TargetHeatingCoolingMode;
import com.automaton.characteristics.AbstractHeatingCoolingMode.ThermostatModeCharacteristic;
import com.automaton.characteristics.AbstractIntegerCharacteristic.*;
import com.automaton.characteristics.AbstractStaticCharacteristic.Manufacturer;
import com.automaton.characteristics.AbstractStaticCharacteristic.Model;
import com.automaton.characteristics.AbstractStaticCharacteristic.SerialNumber;
import com.automaton.characteristics.AbstractTemperature;
import com.automaton.characteristics.AbstractTemperature.CoolingThresholdTemperature;
import com.automaton.characteristics.AbstractTemperature.Temperature;
import com.automaton.characteristics.CharacteristicCallback;
import com.automaton.characteristics.CharacteristicStates.*;

public class AccessoryServices {
    public static class AccessoryInformationService extends AbstractAccessoryService {
        public AccessoryInformationService(Accessory accessory) throws Exception {
            this(accessory, accessory.getLabel());
        }

        public AccessoryInformationService(Accessory accessory, String serviceName) throws Exception {
            super("0000003E-0000-1000-8000-0026BB765291", accessory, serviceName);
            addCharacteristic(new Manufacturer(accessory));
            addCharacteristic(new Model(accessory));
            addCharacteristic(new SerialNumber(accessory));
            addCharacteristic(new Identify(accessory));
        }
    }

    public static class CarbonMonoxideSensorService extends AbstractAccessoryService {
        public CarbonMonoxideSensorService(CarbonMonoxideSensor carbonMonoxideSensor) {
            this(carbonMonoxideSensor, carbonMonoxideSensor.getLabel());
        }

        public CarbonMonoxideSensorService(CarbonMonoxideSensor carbonMonoxideSensor, String serviceName) {
            super("0000007F-0000-1000-8000-0026BB765291", carbonMonoxideSensor, serviceName);
            addCharacteristic(new CarbonMonoxideDetected(carbonMonoxideSensor));
        }
    }

    public static class ContactSensorService extends AbstractAccessoryService {
        public ContactSensorService(ContactSensor contactSensor) {
            this(contactSensor, contactSensor.getLabel());
        }

        public ContactSensorService(ContactSensor contactSensor, String serviceName) {
            super("00000080-0000-1000-8000-0026BB765291", contactSensor, serviceName);
            addCharacteristic(new ContactSensorState(contactSensor));
        }
    }

    public static class FanService extends AbstractAccessoryService {
        public FanService(Fan fan) {
            this(fan, fan.getLabel());
        }

        public FanService(Fan fan, String serviceName) {
            super("00000040-0000-1000-8000-0026BB765291", fan, serviceName);
            addCharacteristic(new PowerState(() -> fan.getFanPower(), v -> fan.setFanPower(v), c -> fan.subscribe(c), () -> fan.unsubscribe()));
            addCharacteristic(new FanDirection(fan));
            addCharacteristic(new FanSpeed(fan));
        }
    }

    public static class GarageDoorService extends AbstractAccessoryService {
        public GarageDoorService(GarageDoor door) {
            this(door, door.getLabel());
        }

        public GarageDoorService(GarageDoor door, String serviceName) {
            super("00000041-0000-1000-8000-0026BB765291", door, serviceName);
            addCharacteristic(new GarageDoorState(door));
            addCharacteristic(new GarageTargetDoorState(door));
            addCharacteristic(new ObstructionDetected(() -> door.getObstructionDetected(), c -> door.subscribeObstructionDetected(c), () -> door.unsubscribeObstructionDetected()));
        }
    }

    public static class HumiditySensorService extends AbstractAccessoryService {
        public HumiditySensorService(HumiditySensor sensor) {
            this(sensor, sensor.getLabel());
        }

        public HumiditySensorService(HumiditySensor sensor, String serviceName) {
            super("00000082-0000-1000-8000-0026BB765291", sensor, serviceName);
            addCharacteristic(new Humidity(sensor));
        }
    }

    public static class LightSensorService extends AbstractAccessoryService {
        public LightSensorService(LightSensor lightSensor) {
            this(lightSensor, lightSensor.getLabel());
        }

        public LightSensorService(LightSensor lightSensor, String serviceName) {
            super("00000084-0000-1000-8000-0026BB765291", lightSensor, serviceName);
            addCharacteristic(new AmbientLightLevelCharacteristic(lightSensor));
        }
    }

    public static class LightService extends AbstractAccessoryService {
        public LightService(Light light) {
            this(light, light.getLabel());
        }

        public LightService(Light light, String serviceName) {
            super("00000043-0000-1000-8000-0026BB765291", light, serviceName);
            addCharacteristic(new PowerState(() -> light.getPowerState(), v -> light.setPowerState(v), c -> light.subscribe(c),
                    () -> light.unsubscribe()));

            if (light instanceof DimmableLight) {
                addCharacteristic(new Brightness((DimmableLight) light));
            }

            if (light instanceof ColorfulLight) {
                addCharacteristic(new Hue((ColorfulLight) light));
                addCharacteristic(new Saturation((ColorfulLight) light));
            }
        }
    }

    public static class LockMechanismService extends AbstractAccessoryService {
        public LockMechanismService(LockMechanism lock) {
            this(lock, lock.getLabel());
        }

        public LockMechanismService(LockMechanism lock, String serviceName) {
            super("00000045-0000-1000-8000-0026BB765291", lock, serviceName);
            addCharacteristic(new LockState(lock));

            if (lock instanceof LockableLockMechanism) {
                addCharacteristic(new TargetLockState((LockableLockMechanism) lock));
            }
        }
    }

    public static class MotionSensorService extends AbstractAccessoryService {
        public MotionSensorService(MotionSensor motionSensor) {
            this(motionSensor, motionSensor.getLabel());
        }

        public MotionSensorService(MotionSensor motionSensor, String serviceName) {
            super("00000085-0000-1000-8000-0026BB765291", motionSensor, serviceName);
            addCharacteristic(new MotionDetectedState(motionSensor));
        }
    }

    public static class OutletService extends AbstractAccessoryService {
        public OutletService(Outlet outlet) {
            this(outlet, outlet.getLabel());
        }

        public OutletService(Outlet outlet, String serviceName) {
            super("00000047-0000-1000-8000-0026BB765291", outlet, serviceName);
            addCharacteristic(new PowerState(() -> outlet.getPowerState(), v -> outlet.setPowerState(v), c -> outlet.subscribe(c), () -> outlet.unsubscribe()));
            addCharacteristic(new OutletInUse(outlet));
        }
    }

    public static class SecuritySystemService extends AbstractAccessoryService {
        public SecuritySystemService(SecuritySystem securitySystem) {
            this(securitySystem, securitySystem.getLabel());
        }

        public SecuritySystemService(SecuritySystem securitySystem, String serviceName) {
            super("0000007E-0000-1000-8000-0026BB765291", securitySystem, serviceName);
            addCharacteristic(new SecuritySystemState(securitySystem));
            addCharacteristic(new TargetSecuritySystemStateCharacteristic(securitySystem));
            addCharacteristic(new SecuritySystemAlarmTypeCharacteristic(securitySystem));
        }
    }

    public static class SmokeSensorService extends AbstractAccessoryService {
        public SmokeSensorService(SmokeSensor smokeSensor) {
            this(smokeSensor, smokeSensor.getLabel());
        }

        public SmokeSensorService(SmokeSensor smokeSensor, String serviceName) {
            super("00000087-0000-1000-8000-0026BB765291", smokeSensor, serviceName);
            addCharacteristic(new SmokeDetected(smokeSensor));
        }
    }

    public static class SwitchService extends AbstractAccessoryService {
        public SwitchService(Switch switchAccessory) {
            this(switchAccessory, switchAccessory.getLabel());
        }

        public SwitchService(Switch switchAccessory, String serviceName) {
            super("00000049-0000-1000-8000-0026BB765291", switchAccessory, serviceName);
            addCharacteristic(new PowerState(() -> switchAccessory.getSwitchState(), v -> switchAccessory.setSwitchState(v), c -> switchAccessory.subscribe(c),
                    () -> switchAccessory.unsubscribe()));
        }
    }

    public static class TemperatureSensorService extends AbstractAccessoryService {
        public TemperatureSensorService(TemperatureSensor sensor) {
            this(sensor, sensor.getLabel());
        }

        public TemperatureSensorService(TemperatureSensor sensor, String serviceName) {
            super("0000008A-0000-1000-8000-0026BB765291", sensor, serviceName);
            addCharacteristic(new Temperature(sensor));
        }
    }

    public static class ThermostatService extends AbstractAccessoryService {
        public ThermostatService(Thermostats thermostat) {
            this(thermostat, thermostat.getLabel());
        }

        public ThermostatService(Thermostats thermostat, String serviceName) {
            super("0000004A-0000-1000-8000-0026BB765291", thermostat, serviceName);
            addCharacteristic(new ThermostatModeCharacteristic(thermostat));
            addCharacteristic(new Temperature(thermostat));
            addCharacteristic(new TargetHeatingCoolingMode(thermostat));
            addCharacteristic(new TemperatureTarget(thermostat));
            addCharacteristic(new TemperatureUnit(thermostat));
            if (thermostat instanceof HeatingThermostat) {
                addCharacteristic(new ThermostatThreshold((HeatingThermostat) thermostat));
            }
            if (thermostat instanceof CoolingThermostat) {
                addCharacteristic(new CoolingThresholdTemperature((CoolingThermostat) thermostat));
            }
        }
    }

    public static class WindowCoveringService extends AbstractAccessoryService {
        public WindowCoveringService(WindowCovering windowCovering) {
            this(windowCovering, windowCovering.getLabel());
        }

        public WindowCoveringService(WindowCovering windowCovering, String serviceName) {
            super("0000008C-0000-1000-8000-0026BB765291", windowCovering, serviceName);
            addCharacteristic(new Position(windowCovering));
            addCharacteristic(new HoldPosition(windowCovering));
            addCharacteristic(new PositionState(windowCovering));
            addCharacteristic(new TargetPosition(windowCovering));
            addCharacteristic(new ObstructionDetected(() -> windowCovering.getObstructionDetected(), c -> windowCovering.subscribeObstructionDetected(c),
                    () -> windowCovering.unsubscribeObstructionDetected()));

            if (windowCovering instanceof HorizontalTiltingWindowCovering) {
                addCharacteristic(new HorizontalTiltAngle((HorizontalTiltingWindowCovering) windowCovering));
                addCharacteristic(new TargetHorizontalTiltAngle((HorizontalTiltingWindowCovering) windowCovering));
            }
            if (windowCovering instanceof VerticalTiltingWindowCovering) {
                addCharacteristic(new VerticalTiltAngle((VerticalTiltingWindowCovering) windowCovering));
                addCharacteristic(new VerticalTiltAngleTarget((VerticalTiltingWindowCovering) windowCovering));
            }
        }
    }

    public static class TemperatureTarget extends AbstractTemperature {
        private final Thermostats thermostat;

        public TemperatureTarget(Thermostats thermostat) {
            super("00000035-0000-1000-8000-0026BB765291", true, "Target Temperature", thermostat);
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
            return thermostat.getTargetTemperature();
        }

        @Override
        protected void setValue(Double value) throws Exception {
            thermostat.setTargetTemperature(value);
        }
    }

    public static class ThermostatThreshold extends AbstractTemperature {
        private final HeatingThermostat thermostat;

        public ThermostatThreshold(HeatingThermostat thermostat) {
            super("00000012-0000-1000-8000-0026BB765291", true, "Temperature below which heating will be active", thermostat);
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
            return thermostat.getHeatingThresholdTemperature();
        }

        @Override
        protected void setValue(Double value) throws Exception {
            thermostat.setHeatingThresholdTemperature(value);
        }
    }
}
