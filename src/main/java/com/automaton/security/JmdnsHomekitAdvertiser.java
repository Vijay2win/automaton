package com.automaton.security;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmdnsHomekitAdvertiser {
    private static final String SERVICE_TYPE = "_hap._tcp.local.";
    private final JmDNS jmdns;
    private boolean discoverable = true;
    private static final Logger logger = LoggerFactory.getLogger(JmdnsHomekitAdvertiser.class);

    private boolean isAdvertising = false;

    private String label;
    private String mac;
    private int port;
    private int configurationIndex;

    public JmdnsHomekitAdvertiser(InetAddress localAddress) throws UnknownHostException, IOException {
        this.jmdns = JmDNS.create(localAddress);
    }

    public synchronized void advertise(String label, String mac, int port, int configurationIndex) throws Exception {
        if (this.isAdvertising) {
            throw new IllegalStateException("Homekit advertiser is already running");
        }
        this.label = label;
        this.mac = mac;
        this.port = port;
        this.configurationIndex = configurationIndex;

        logger.info("Advertising accessory " + label);

        registerService();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Stopping advertising in response to shutdown.");
            this.jmdns.unregisterAllServices();
        }));
        this.isAdvertising = true;
    }

    public synchronized void stop() {
        this.jmdns.unregisterAllServices();
    }

    public synchronized void setDiscoverable(boolean discoverable) throws IOException {
        if (this.discoverable != discoverable) {
            this.discoverable = discoverable;
            if (this.isAdvertising) {
                logger.info("Re-creating service due to change in discoverability to " + discoverable);
                this.jmdns.unregisterAllServices();
                registerService();
            }
        }
    }

    public synchronized void setConfigurationIndex(int revision) throws IOException {
        if (this.configurationIndex != revision) {
            this.configurationIndex = revision;
            if (this.isAdvertising) {
                logger.info("Re-creating service due to change in configuration index to " + revision);
                this.jmdns.unregisterAllServices();
                registerService();
            }
        }
    }

    private void registerService() throws IOException {
        logger.info("Registering _hap._tcp.local. on port " + this.port);
        Map<String, String> props = new HashMap<>();
        props.put("sf", this.discoverable ? "1" : "0");
        props.put("id", this.mac);
        props.put("md", this.label);
        props.put("c#", Integer.toString(this.configurationIndex));
        props.put("s#", "1");
        props.put("ff", "0");
        props.put("ci", "1");
        this.jmdns.registerService(ServiceInfo.create("_hap._tcp.local.", this.label, this.port, 1, 1, props));
    }
}
