package com.automaton.myq;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRoot;
import com.automaton.characteristics.CharacteristicCallback;
import com.automaton.server.AutomatonConfiguration;
import com.automaton.server.DeviceDriver;

public class GarageDoorDriver implements DeviceDriver {
    protected static final Logger logger = LoggerFactory.getLogger(GarageDoorDriver.class);
    
    private static final String USER_NAME = AutomatonConfiguration.getString("automaton.myq.user_name", "vijay2win@yahoo.com");
    private static final String PASSWORD = AutomatonConfiguration.getString("automaton.myq.password", "xxxxx");
    private final GarageDoorHub door = new GarageDoorHub(USER_NAME, PASSWORD);
    
    private final HomekitRoot bridge;

    public GarageDoorDriver(HomekitRoot bridge) {
        this.bridge = bridge;
        logger.info("Trying to intialize MyQ Garage Device");
    }

    public static CharacteristicCallback callback(final GarageDoorDevice device) {
        return new CharacteristicCallback() {
            public void changed() {
                try {
                    logger.info("Adding Garagedoor to the Homekit hub {}", device.getCurrentDoorState().get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    logger.error("error message recived", e);
                }
            }

            public boolean isRemovable() {
                return false;
            }
        };
    }

    @Override
    public void initialize() throws Exception {
        List<GarageDoorDevice> devices = door.init();
        for (GarageDoorDevice device: devices) {
            device.subscribe(callback(device));
            bridge.addAccessory(device);
            logger.info("Adding Garagedoor to the Homekit hub {}", device);
        }        
    }
}
