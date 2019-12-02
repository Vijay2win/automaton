package com.automaton.characteristics;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.automaton.accessories.*;
import com.automaton.utils.ExceptionalConsumer;

public class CharacteristicStates {
    public static class LockState extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final LockMechanism lock;

        public LockState(LockMechanism lock) {
            super("0000001D-0000-1000-8000-0026BB765291", false, true, "Current lock state", 3);
            this.lock = lock;
        }

        protected void setValue(Integer value) throws Exception {
        }

        protected CompletableFuture<Integer> getValue() {
            return this.lock.getState().thenApply(s -> Integer.valueOf(s.getCode()));
        }

        public void subscribe(CharacteristicCallback callback) {
            this.lock.subscribe(callback);
        }

        public void unsubscribe() {
            this.lock.unsubscribe();
        }
    }

    public static class PositionState extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final WindowCovering windowCovering;

        public PositionState(WindowCovering windowCovering) {
            super("00000072-0000-1000-8000-0026BB765291", false, true, "The position state", 2);
            this.windowCovering = windowCovering;
        }

        protected void setValue(Integer value) throws Exception {
        }

        protected CompletableFuture<Integer> getValue() {
            return this.windowCovering.getPositionState().thenApply(v -> Integer.valueOf(v.getCode()));
        }

        public void subscribe(CharacteristicCallback callback) {
            this.windowCovering.subscribePositionState(callback);
        }

        public void unsubscribe() {
            this.windowCovering.unsubscribePositionState();
        }
    }

    public static class PowerState extends AbstractCharacteristic.BooleanCharacteristic
            implements EventableCharacteristic {
        private final Supplier<CompletableFuture<Boolean>> getter;
        private final ExceptionalConsumer<Boolean> setter;
        private final Consumer<CharacteristicCallback> subscriber;
        private final Runnable unsubscriber;

        public PowerState(Supplier<CompletableFuture<Boolean>> getter, ExceptionalConsumer<Boolean> setter,
                Consumer<CharacteristicCallback> subscriber, Runnable unsubscriber) {
            super("00000025-0000-1000-8000-0026BB765291", true, true, "Turn on and off");
            this.getter = getter;
            this.setter = setter;
            this.subscriber = subscriber;
            this.unsubscriber = unsubscriber;
        }

        public void setValue(Boolean value) throws Exception {
            this.setter.accept(value);
        }

        protected CompletableFuture<Boolean> getValue() {
            return this.getter.get();
        }

        public void subscribe(CharacteristicCallback callback) {
            this.subscriber.accept(callback);
        }

        public void unsubscribe() {
            this.unsubscriber.run();
        }
    }

    public static class SecuritySystemState extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final SecuritySystem securitySystem;

        public SecuritySystemState(SecuritySystem securitySystem) {
            super("00000066-0000-1000-8000-0026BB765291", false, true, "Current security system state", 4);
            this.securitySystem = securitySystem;
        }

        protected CompletableFuture<Integer> getValue() {
            return this.securitySystem.getCurrentSecuritySystemState()
                    .thenApply(SecuritySystem.CurrentSecuritySystemState::getCode);
        }

        protected void setValue(Integer value) throws Exception {
        }

        public void subscribe(CharacteristicCallback callback) {
            this.securitySystem.subscribeCurrentSecuritySystemState(callback);
        }

        public void unsubscribe() {
            this.securitySystem.unsubscribeCurrentSecuritySystemState();
        }
    }

    public static class SmokeDetected extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final SmokeSensor smokeSensor;

        public SmokeDetected(SmokeSensor smokeSensor) {
            super("00000076-0000-1000-8000-0026BB765291", false, true, "Smoke Detected", 1);
            this.smokeSensor = smokeSensor;
        }

        protected CompletableFuture<Integer> getValue() {
            return this.smokeSensor.getSmokeDetectedState().thenApply(SmokeSensor.SmokeDetectedState::getCode);
        }

        protected void setValue(Integer value) throws Exception {
        }

        public void subscribe(CharacteristicCallback callback) {
            this.smokeSensor.subscribe(callback);
        }

        public void unsubscribe() {
            this.smokeSensor.unsubscribe();
        }
    }

    public static class TargetLockState extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final LockMechanism.LockableLockMechanism lock;

        public TargetLockState(LockMechanism.LockableLockMechanism lock) {
            super("0000001E-0000-1000-8000-0026BB765291", true, true, "Current lock state", 3);
            this.lock = lock;
        }

        protected void setValue(Integer value) throws Exception {
            this.lock.setTargetMechanismState(LockMechanism.LockMechanismState.fromCode(value));
        }

        protected CompletableFuture<Integer> getValue() {
            return this.lock.getTargetMechanismState().thenApply(s -> Integer.valueOf(s.getCode()));
        }

        public void subscribe(CharacteristicCallback callback) {
            this.lock.subscribe(callback);
        }

        public void unsubscribe() {
            this.lock.unsubscribe();
        }
    }

    public static class TargetSecuritySystemStateCharacteristic extends AbstractEnumCharacteristic
            implements EventableCharacteristic {
        private final SecuritySystem securitySystem;

        public TargetSecuritySystemStateCharacteristic(SecuritySystem securitySystem) {
            super("00000067-0000-1000-8000-0026BB765291", true, true, "Target security system state", 3);
            this.securitySystem = securitySystem;
        }

        protected CompletableFuture<Integer> getValue() {
            return this.securitySystem.getTargetSecuritySystemState()
                    .thenApply(SecuritySystem.TargetSecuritySystemState::getCode);
        }

        protected void setValue(Integer value) throws Exception {
            this.securitySystem.setTargetSecuritySystemState(SecuritySystem.TargetSecuritySystemState.fromCode(value));
        }

        public void subscribe(CharacteristicCallback callback) {
            this.securitySystem.subscribeTargetSecuritySystemState(callback);
        }

        public void unsubscribe() {
            this.securitySystem.unsubscribeTargetSecuritySystemState();
        }
    }

    public static class TemperatureUnit extends AbstractEnumCharacteristic {
        private final TemperatureSensor.Thermostats thermostat;

        public TemperatureUnit(TemperatureSensor.Thermostats thermostat) {
            super("00000036-0000-1000-8000-0026BB765291", false, true, "The temperature unit", 1);
            this.thermostat = thermostat;
        }

        protected void setValue(Integer value) throws Exception {
        }

        protected CompletableFuture<Integer> getValue() {
            return CompletableFuture.completedFuture(Integer.valueOf(this.thermostat.getTemperatureUnit().getCode()));
        }
    }
}
