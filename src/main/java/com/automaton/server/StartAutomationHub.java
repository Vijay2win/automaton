package com.automaton.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRoot;
import com.automaton.HomekitServer;
import com.automaton.zwave.ZWaveDriver;

public class StartAutomationHub {
    private static final Logger logger = LoggerFactory.getLogger(StartAutomationHub.class);
    private static final boolean MOCK_ZWAVE = Boolean.parseBoolean(System.getProperty("mock.zwave", "false"));

    private static final int PORT = 9123;
    private static final String MY_NAME = (MOCK_ZWAVE) ? "HUB-MOCK" : "HUB";
    private static final String CORP = "Automaton, Inc.";
    private static final String MODEL = "RPi";
    private static final String SERIAL_NUMBER = "111abe234";

    private static StartAutomationHub SERVER;

    private final HomekitRoot bridge;
    private final DeviceDriver driver;

    public StartAutomationHub() throws Exception {
        this.bridge = new HomekitServer(PORT).createBridge(MY_NAME, CORP, MODEL, SERIAL_NUMBER);
        this.driver = new ZWaveDriver(bridge);
        // this.driver = new MockDriver(bridge);
        // this.driver = new ZWaveDriver2(bridge);
    }

    private void init() throws Exception {
        driver.initializeZWave();

        bridge.start();
        logger.info("Homekit network is ready to be paired.");
    }

    public static void main(String[] args) {
        try {
            logger.info("Starting Home Automation Hub by Vijay...");
            SERVER = new StartAutomationHub();
            SERVER.init();
        } catch (Throwable e) {
            logger.error("Error in adding the device to bridge...", e);
            System.exit(-1);
        }
        if (!MOCK_ZWAVE) {
            System.out.close();
            System.err.close();
        }
    }
}
