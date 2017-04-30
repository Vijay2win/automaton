package com.automaton.characteristics;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.automaton.accessories.*;
import com.automaton.accessories.LockMechanism.LockMechanismState;
import com.automaton.accessories.LockMechanism.LockableLockMechanism;
import com.automaton.accessories.SecuritySystem.CurrentSecuritySystemState;
import com.automaton.accessories.SecuritySystem.TargetSecuritySystemState;
import com.automaton.accessories.SmokeSensor.SmokeDetectedState;
import com.automaton.accessories.TemperatureSensor.Thermostats;
import com.automaton.characteristics.AbstractCharacteristic.BooleanCharacteristic;
import com.automaton.utils.ExceptionalConsumer;

public class CharacteristicStates {
    public static class LockState extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final LockMechanism lock;

        public LockState(LockMechanism lock) {
            super("0000001D-0000-1000-8000-0026BB765291", false, true, "Current lock state", 3);
            this.lock = lock;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Not writable
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return lock.getState().thenApply(s -> s.getCode());
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            lock.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            lock.unsubscribe();
        }
    }

    public static class PositionState extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final WindowCovering windowCovering;

        public PositionState(WindowCovering windowCovering) {
            super("00000072-0000-1000-8000-0026BB765291", false, true, "The position state", 2);
            this.windowCovering = windowCovering;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Read only
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return windowCovering.getPositionState().thenApply(v -> v.getCode());
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            windowCovering.subscribePositionState(callback);
        }

        @Override
        public void unsubscribe() {
            windowCovering.unsubscribePositionState();
        }
    }

    public static class PowerState extends BooleanCharacteristic implements EventableCharacteristic {
        private final Supplier<CompletableFuture<Boolean>> getter;
        private final ExceptionalConsumer<Boolean> setter;
        private final Consumer<CharacteristicCallback> subscriber;
        private final Runnable unsubscriber;

        public PowerState(Supplier<CompletableFuture<Boolean>> getter, ExceptionalConsumer<Boolean> setter, Consumer<CharacteristicCallback> subscriber, Runnable unsubscriber) {
            super("00000025-0000-1000-8000-0026BB765291", true, true, "Turn on and off");
            this.getter = getter;
            this.setter = setter;
            this.subscriber = subscriber;
            this.unsubscriber = unsubscriber;
        }

        @Override
        public void setValue(Boolean value) throws Exception {
            setter.accept(value);
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

    public static class SecuritySystemState extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final SecuritySystem securitySystem;

        public SecuritySystemState(SecuritySystem securitySystem) {
            super("00000066-0000-1000-8000-0026BB765291", false, true, "Current security system state", 4);
            this.securitySystem = securitySystem;
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return securitySystem.getCurrentSecuritySystemState().thenApply(CurrentSecuritySystemState::getCode);
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Not writable
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            securitySystem.subscribeCurrentSecuritySystemState(callback);
        }

        @Override
        public void unsubscribe() {
            securitySystem.unsubscribeCurrentSecuritySystemState();
        }
    }

    public static class SmokeDetected extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final SmokeSensor smokeSensor;

        public SmokeDetected(SmokeSensor smokeSensor) {
            super("00000076-0000-1000-8000-0026BB765291", false, true, "Smoke Detected", 1);
            this.smokeSensor = smokeSensor;
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return smokeSensor.getSmokeDetectedState().thenApply(SmokeDetectedState::getCode);
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Read Only
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            smokeSensor.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            smokeSensor.unsubscribe();
        }
    }

    public static class TargetLockState extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final LockableLockMechanism lock;

        public TargetLockState(LockableLockMechanism lock) {
            super("0000001E-0000-1000-8000-0026BB765291", true, true, "Current lock state", 3);
            this.lock = lock;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            lock.setTargetMechanismState(LockMechanismState.fromCode(value));
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return lock.getTargetMechanismState().thenApply(s -> s.getCode());
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            lock.subscribe(callback);
        }

        @Override
        public void unsubscribe() {
            lock.unsubscribe();
        }
    }

    public static class TargetSecuritySystemStateCharacteristic extends AbstractEnumCharacteristic implements EventableCharacteristic {
        private final SecuritySystem securitySystem;

        public TargetSecuritySystemStateCharacteristic(SecuritySystem securitySystem) {
            super("00000067-0000-1000-8000-0026BB765291", true, true, "Target security system state", 3);
            this.securitySystem = securitySystem;
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return securitySystem.getTargetSecuritySystemState().thenApply(TargetSecuritySystemState::getCode);
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            securitySystem.setTargetSecuritySystemState(TargetSecuritySystemState.fromCode(value));
        }

        @Override
        public void subscribe(CharacteristicCallback callback) {
            securitySystem.subscribeTargetSecuritySystemState(callback);
        }

        @Override
        public void unsubscribe() {
            securitySystem.unsubscribeTargetSecuritySystemState();
        }
    }

    public static class TemperatureUnit extends AbstractEnumCharacteristic {
        private final Thermostats thermostat;

        public TemperatureUnit(Thermostats thermostat) {
            super("00000036-0000-1000-8000-0026BB765291", false, true, "The temperature unit", 1);
            this.thermostat = thermostat;
        }

        @Override
        protected void setValue(Integer value) throws Exception {
            // Not writable
        }

        @Override
        protected CompletableFuture<Integer> getValue() {
            return CompletableFuture.completedFuture(thermostat.getTemperatureUnit().getCode());
        }
    }
}
