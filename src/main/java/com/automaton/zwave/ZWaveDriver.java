package com.automaton.zwave;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRoot;
import com.automaton.characteristics.CharacteristicCallback;
import com.automaton.server.AutomatonConfiguration;
import com.automaton.server.DeviceDriver;
import com.automaton.zwave.AbstractZSwitch.ZOnOffSwitch;
import com.oberasoftware.home.zwave.api.LocalZwaveSession;
import com.oberasoftware.home.zwave.api.ZWaveSession;
import com.oberasoftware.home.zwave.api.actions.DeviceManufactorAction;
import com.oberasoftware.home.zwave.api.actions.IdentifyNodeAction;
import com.oberasoftware.home.zwave.api.actions.SwitchAction;
import com.oberasoftware.home.zwave.api.actions.SwitchAction.STATE;
import com.oberasoftware.home.zwave.api.messages.types.CommandClass;
import com.oberasoftware.home.zwave.core.NodeAvailability;
import com.oberasoftware.home.zwave.core.ZWaveNode;

public class ZWaveDriver implements DeviceDriver {
    protected static final Logger logger = LoggerFactory.getLogger(ZWaveDriver.class);
    private final ZWaveSession session;
    private final HomekitRoot bridge;

    public ZWaveDriver(HomekitRoot bridge) {
        this.bridge = bridge;
        logger.info("Trying to intialize z-wave network");
        this.session = new LocalZwaveSession().connect();
    }

    public void initializeZWave() throws Exception {
        while (!session.isNetworkReady()) {
            logger.info("ZWave Network not ready yet, sleeping");
            sleepUninterruptibly(1, TimeUnit.SECONDS);
        }

        logger.info("ZWave Network is ready");
        sleepUninterruptibly(5, SECONDS);

        List<ZWaveNode> nodes = session.getDeviceManager().getNodes().stream()
                .map(node -> initialize(node))
                .filter(node -> node.getNodeInformation().isPresent())
                .collect(Collectors.toList());

        nodes.forEach(node -> {
            String name = AutomatonConfiguration.getString("device.name." + node.getNodeId(), "z-wave," + node.getNodeId());
            switch (node.getNodeInformation().get().getGenericDeviceClass()) {
            case BINARY_SWITCH:
                ZOnOffSwitch ooSwitch = new ZOnOffSwitch(node.getNodeId(), name);
                ooSwitch.subscribe(callback(ooSwitch, session));
                bridge.addAccessory(ooSwitch);
                break;
            case MULTILEVEL_SWITCH:
                ZDimmableSwitch dimmable = new ZDimmableSwitch(node.getNodeId(), name);
                dimmable.subscribe(callback(dimmable, session));
                bridge.addAccessory(dimmable);
                break;
            default:
                logger.info("Device type unknown: {}", node.getNodeId());
            }
        });
    }

    private ZWaveNode initialize(ZWaveNode node) {
        try {
            while (node.getAvailability() != NodeAvailability.AVAILABLE) {
                int nodeId = node.getNodeId();
                session.getDeviceManager().registerCommandClass(nodeId, CommandClass.ALL);
                session.doAction(new DeviceManufactorAction(nodeId));
                session.doAction(new IdentifyNodeAction(nodeId));
                session.getDeviceManager().setNodeAvailability(nodeId, NodeAvailability.AVAILABLE);

                logger.info("Node.toString() -> {}, hence sleeping for 1 Second.", node);
                sleepUninterruptibly(1, TimeUnit.SECONDS);

                node = session.getDeviceManager().getNode(node.getNodeId());
            }
        } catch (Exception ex) {
            logger.error("Exception in initializing the node {}", node);
        }
        logger.info("Node.toString() -> {}", node);
        logger.info("Node.getNodeProperties() {}", node.getNodeProperties());
        return node;
    }

    public static CharacteristicCallback callback(ZDimmableSwitch sw, ZWaveSession session) {
        return new CharacteristicCallback() {
            public void changed() {
                ZWaveNode node = session.getDeviceManager().getNode(sw.getId());
                try {
                    if (!sw.powerState) {
                        session.doAction(new SwitchAction(node.getNodeId(), 0, STATE.ON, 0));
                        return;
                    }

                    if (sw.brightness == 0)
                        sw.brightness = 20;
                    if (sw.brightness == 100)
                        sw.brightness = 99;

                    session.doAction(new SwitchAction(node.getNodeId(), 0, STATE.ON, sw.brightness));
                } catch (Throwable e) {
                    logger.error("Exception in changing the state. node with id {}", node.getNodeId(), e);
                }
            }

            public boolean isRemovable() {
                return false;
            }
        };
    }

    public CharacteristicCallback callback(ZColorfulSwitch sw, ZWaveSession session) {
        return new CharacteristicCallback() {
            public void changed() {
                try {
                    if (!sw.powerState) {
                        session.doAction(new SwitchAction(sw.getId(), 0, STATE.OFF, 0));
                        return;
                    }

                    if (sw.brightness == 0)
                        sw.brightness = 50d;

                    session.doAction(new SwitchAction(sw.getId(), 0, STATE.ON, sw.brightness.intValue()));
                } catch (Throwable e) {
                    logger.error("Exception in changing the state.", e);
                }
            }

            public boolean isRemovable() {
                return false;
            }
        };
    }

    public CharacteristicCallback callback(ZOnOffSwitch sw, ZWaveSession session) {
        return new CharacteristicCallback() {
            public void changed() {
                try {
                    session.doAction(new SwitchAction(sw.getId(), (sw.powerState) ? STATE.ON : STATE.OFF));
                } catch (Throwable th) {
                    logger.error("Exception in changing the state.", th);
                }
            }

            public boolean isRemovable() {
                return false;
            }
        };
    }
}
