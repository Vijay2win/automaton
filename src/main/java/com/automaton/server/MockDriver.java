package com.automaton.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRoot;
import com.automaton.accessories.Accessory;
import com.automaton.zwave.AbstractZSwitch;
import com.automaton.zwave.ZDimmableSwitch;

public class MockDriver implements DeviceDriver {
    protected static final Logger logger = LoggerFactory.getLogger(MockDriver.class);

    private final HomekitRoot bridge;

    public MockDriver(HomekitRoot bridge) {
        this.bridge = bridge;
    }

    public void initialize() throws Exception {
        this.bridge.addAccessory((Accessory) new ZDimmableSwitch(7, "1233"));

        this.bridge.addAccessory((Accessory) new AbstractZSwitch.ZOnOffSwitch(6, "1234"));
        this.bridge.addAccessory((Accessory) new AbstractZSwitch.ZOnOffSwitch(5, "1235"));
        this.bridge.addAccessory((Accessory) new AbstractZSwitch.ZOnOffSwitch(4, "1236"));
    }
}
