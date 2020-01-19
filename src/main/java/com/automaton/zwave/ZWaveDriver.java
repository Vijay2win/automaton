package com.automaton.zwave;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRoot;
import com.automaton.characteristics.CharacteristicCallback;
import com.automaton.server.AutomatonConfiguration;
import com.automaton.server.DeviceDriver;
import com.google.common.util.concurrent.Uninterruptibles;
import com.oberasoftware.home.zwave.api.LocalZwaveSession;
import com.oberasoftware.home.zwave.api.ZWaveSession;
import com.oberasoftware.home.zwave.api.actions.*;
import com.oberasoftware.home.zwave.api.actions.SwitchAction.STATE;
import com.oberasoftware.home.zwave.api.events.NodeIdentifyEvent;
import com.oberasoftware.home.zwave.api.messages.types.CommandClass;
import com.oberasoftware.home.zwave.api.messages.types.GenericDeviceClass;
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

    public void initialize() throws Exception {
        while (!this.session.isNetworkReady()) {
            logger.info("ZWave Network not ready yet, sleeping");
            Uninterruptibles.sleepUninterruptibly(1L, TimeUnit.SECONDS);
        }

        logger.info("ZWave Network is ready");
        Uninterruptibles.sleepUninterruptibly(5L, TimeUnit.SECONDS);

        for (ZWaveNode node : this.session.getDeviceManager().getNodes()) {
            initialize(node);
            logger.info("node properties {}", node.getNodeProperties());
            String name = AutomatonConfiguration.getString("device.name." + node.getNodeId(), "z-wave," + node.getNodeId());

            Optional<NodeIdentifyEvent> info = node.getNodeInformation();
            if (!info.isPresent())
                return;

            if (info.get().getGenericDeviceClass() == GenericDeviceClass.MULTILEVEL_SWITCH) {
                ZDimmableSwitch dimmable = new ZDimmableSwitch(node.getNodeId(), name);
                dimmable.subscribe(callback(dimmable, this.session));
                this.bridge.addAccessory(dimmable);
            } else if (info.get().getGenericDeviceClass() == GenericDeviceClass.BINARY_SWITCH) {
                AbstractZSwitch.ZOnOffSwitch ooSwitch = new AbstractZSwitch.ZOnOffSwitch(node.getNodeId(), name);
                ooSwitch.subscribe(callback(ooSwitch, this.session));
                this.bridge.addAccessory(ooSwitch);
            }
        }
    }

    private ZWaveNode initialize(ZWaveNode node) throws Exception {
        while (node.getAvailability() != NodeAvailability.AVAILABLE) {
            int nodeId = node.getNodeId();
            this.session.getDeviceManager().registerCommandClass(nodeId, CommandClass.ALL);
            this.session.doAction(new DeviceManufactorAction(nodeId));
            this.session.doAction(new IdentifyNodeAction(nodeId));
            this.session.doAction(new SwitchMultiLevelGetAction(nodeId));
            this.session.getDeviceManager().setNodeAvailability(nodeId, NodeAvailability.AVAILABLE);

            logger.info("Node.toString() -> {}, hence sleeping for 1 Second.", node);
            Uninterruptibles.sleepUninterruptibly(1L, TimeUnit.SECONDS);

            node = this.session.getDeviceManager().getNode(node.getNodeId());
        }
        logger.info("Node.toString() -> {}", node);
        return node;
    }

    public static CharacteristicCallback callback(final ZDimmableSwitch sw, final ZWaveSession session) {
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
                    if (sw.brightness == 100) {
                        sw.brightness = 99;
                    }
                    session.doAction(new SwitchAction(node.getNodeId(), 0, STATE.ON, sw.brightness));
                    session.doAction(new SwitchMultiLevelGetAction(node.getNodeId()));
                } catch (Throwable e) {
                    logger.error("Exception in changing the state. node with id {}", node.getNodeId(), e);
                }
            }

            public boolean isRemovable() {
                return false;
            }
        };
    }

    public CharacteristicCallback callback(final ZColorfulSwitch sw, final ZWaveSession session) {
        return new CharacteristicCallback() {
            public void changed() {
                try {
                    if (!sw.powerState) {
                        session.doAction(new SwitchAction(sw.getId(), 0, STATE.OFF, 0));
                        return;
                    }

                    if (sw.powerState && sw.brightness == 0D)
                        sw.brightness = 99D;

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

    public CharacteristicCallback callback(final AbstractZSwitch.ZOnOffSwitch sw, final ZWaveSession session) {
        return new CharacteristicCallback() {
            public void changed() {
                try {
                    session.doAction(new SwitchAction(sw.getId(), sw.powerState ? STATE.ON : STATE.OFF));
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
