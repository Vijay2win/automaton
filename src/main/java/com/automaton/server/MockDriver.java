package com.automaton.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRoot;
import com.automaton.zwave.AbstractZSwitch.ZOnOffSwitch;
import com.automaton.zwave.ZDimmableSwitch;

public class MockDriver implements DeviceDriver {
    protected static final Logger logger = LoggerFactory.getLogger(MockDriver.class);
    private final HomekitRoot bridge;

    public MockDriver(HomekitRoot bridge) {
        this.bridge = bridge;
    }

    public void initializeZWave() throws Exception {
        bridge.addAccessory(new ZDimmableSwitch(7, "1233"));

        bridge.addAccessory(new ZOnOffSwitch(6, "1234"));
        bridge.addAccessory(new ZOnOffSwitch(5, "1235"));
        bridge.addAccessory(new ZOnOffSwitch(4, "1236"));
    }
}
