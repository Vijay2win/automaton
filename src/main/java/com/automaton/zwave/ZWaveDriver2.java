package com.automaton.zwave;
//package com.automaton.server;
//
//import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
//import static java.util.concurrent.TimeUnit.SECONDS;
//
//import java.io.File;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.automaton.CharacteristicCallback;
//import com.automaton.HomekitRoot;
//import com.automaton.HomekitServer;
//import com.automaton.server.AbstractZSwitch.ZOnOffSwitch;
//import com.oberasoftware.home.zwave.api.messages.types.GenericDeviceClass;
//import com.whizzosoftware.wzwave.controller.netty.NettyZWaveController;
//import com.whizzosoftware.wzwave.node.ZWaveNode;
//
//public class ZWaveDriver2 implements ZDriver {
//    protected static final Logger logger = LoggerFactory.getLogger(ZWaveDriver2.class);
//    private final NettyZWaveController session;
//    private final HomekitRoot bridge;
//
//    public ZWaveDriver2(HomekitRoot bridge) {
//        this.bridge = bridge;
//        logger.info("Trying to intialize z-wave network");
//        this.session = new NettyZWaveController("/dev/ttyACM0", new File("/tmp/zwave/"));
//        this.session.start();
//    }
//
//    public void initializeZWave() throws Exception {
//        sleepUninterruptibly(5, SECONDS);
//
//        for (ZWaveNode node : session.getNodes()) {
//            int id = node.getNodeId();
//            String name = HubConfiguration.getString("device.name." + id, "z-wave," + node.getNodeId());
//
//            if (node.getGenericDeviceClass() == GenericDeviceClass.MULTILEVEL_SWITCH.getKey()) {
//                ZColorfulSwitch dimmable = new ZColorfulSwitch(node.getNodeId(), name);
//                dimmable.subscribeLightbulbPowerState(callback(dimmable));
//                bridge.addAccessory(dimmable);
//            } else if (node.getGenericDeviceClass() == GenericDeviceClass.BINARY_SWITCH.getKey()) {
//                ZOnOffSwitch ooSwitch = new ZOnOffSwitch(node.getNodeId(), name);
//                ooSwitch.subscribeLightbulbPowerState(callback(ooSwitch));
//                bridge.addAccessory(ooSwitch);
//            }
//        }
//    }
//
//    public CharacteristicCallback callback(ZDimmableSwitch sw) {
//        return new CharacteristicCallback() {
//            public void changed() {
//                try {
//                    if (sw.powerState && sw.brightness == 0)
//                        sw.brightness = 50;
//                    NettyZWaveController session = ZWaveDriver2.this.session;
//                    ZWaveNode node = session.getNode((byte) sw.getId());
//                    ZWaveDriver2.this.session.onZWaveNodeUpdated(node);
//                } catch (Throwable e) {
//                    logger.error("Exception in changing the state.", e);
//                }
//            }
//        };
//    }
//
//    public CharacteristicCallback callback(ZColorfulSwitch sw) {
//        return new CharacteristicCallback() {
//            public void changed() {
//                try {
//                    if (!sw.powerState) {
//                        return;
//                    }
//
//                    if (sw.powerState && sw.brightness == 0)
//                        sw.brightness = 50d;
//
//                } catch (Throwable e) {
//                    logger.error("Exception in changing the state.", e);
//                }
//            }
//        };
//    }
//
//    public CharacteristicCallback callback(ZOnOffSwitch sw) {
//        return new CharacteristicCallback() {
//            public void changed() {
//                try {
//                } catch (Throwable th) {
//                    logger.error("Exception in changing the state.", th);
//                }
//            }
//        };
//    }
//}
