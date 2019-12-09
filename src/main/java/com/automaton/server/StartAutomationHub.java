package com.automaton.server;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRoot;
import com.automaton.HomekitServer;
import com.automaton.myq.GarageDoorDriver;
import com.automaton.zwave.ZWaveDriver;

public class StartAutomationHub {
    private static final Logger logger = LoggerFactory.getLogger(StartAutomationHub.class);

    private static final String DEFAULT_HUB_NAME = AutomatonConfiguration.getString("automaton.hub.name", "HUB");
    private static final boolean ENABLE_ZWAVE = AutomatonConfiguration.getBoolean("automaton.hub.zwave_enable", true);
    private static final boolean ENABLE_MYQ = AutomatonConfiguration.getBoolean("automaton.hub.myq_enable", true);

    private static final int PORT = AutomatonConfiguration.getInt("automaton.hub.port", 9123);
    private static final String SERIAL_NUMBER = AutomatonConfiguration.getString("automaton.hub.serial_no", "111abe234");
    private static final String MY_NAME = ENABLE_ZWAVE ? DEFAULT_HUB_NAME + "-MOCK" : DEFAULT_HUB_NAME;
    private static final String CORP = "Automaton, Inc & Vijay";
    private static final String MODEL = "RPi";

    private static StartAutomationHub SERVER;

    private final HomekitRoot bridge;
    private final List<DeviceDriver> drivers = new ArrayList<>();

    public StartAutomationHub() throws Exception {
        this.bridge = new HomekitServer(PORT).createBridge(MY_NAME, CORP, MODEL, SERIAL_NUMBER);
        loadPlugins();
    }

    /**
     * TODO load plugin classes from the config...
     */
    private void loadPlugins() {
        this.drivers.add(ENABLE_ZWAVE ? new ZWaveDriver(bridge): new MockDriver(bridge));
        if (ENABLE_MYQ)
            this.drivers.add(new GarageDoorDriver(bridge));        
    }

    private void init() throws Exception {
        for (DeviceDriver driver : drivers) {
            driver.initialize();
        }
        bridge.start();
        logger.info("Homekit network is ready to be paired.");
    }

    public static void main(String[] args) {
        try {
            logger.info("Starting Home Automation Hub by {}", CORP);
            SERVER = new StartAutomationHub();
            SERVER.init();
        } catch (Throwable e) {
            logger.error("Error in adding the devices to {}", MY_NAME, e);
            System.exit(-1);
        }
        System.out.close();
        System.err.close();
    }
}
