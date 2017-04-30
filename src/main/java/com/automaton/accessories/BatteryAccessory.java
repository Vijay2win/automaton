package com.automaton.accessories;

import java.util.concurrent.CompletableFuture;

public interface BatteryAccessory extends Accessory {
    CompletableFuture<Integer> getBatteryLevelState();
}
