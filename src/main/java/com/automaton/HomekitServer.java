package com.automaton;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.util.concurrent.ExecutionException;

import com.automaton.accessories.Accessory;
import com.automaton.http.NettyHttpServer;
import com.automaton.utils.AutomatonUtils;

public class HomekitServer {
    private final NettyHttpServer http;
    private final InetAddress localAddress;

    public HomekitServer(InetAddress localAddress, int port, int nThreads) throws IOException {
        this.localAddress = localAddress;
        this.http = new NettyHttpServer(port, nThreads);
    }

    public HomekitServer(InetAddress localAddress, int port) throws IOException {
        this(localAddress, port, Runtime.getRuntime().availableProcessors());
    }

    public HomekitServer(int port) throws IOException {
        this(InetAddress.getLocalHost(), port);
    }

    public void stop() {
        this.http.shutdown();
    }

    public HomekitStandaloneAccessoryServer createStandaloneAccessory(Accessory accessory) throws IOException {
        return new HomekitStandaloneAccessoryServer(accessory, this.http, this.localAddress);
    }

    public HomekitRoot createBridge(String label, String manufacturer, String model, String serialNumber)
            throws IOException {
        HomekitRoot root = new HomekitRoot(label, this.http, this.localAddress);
        root.addAccessory(new HomekitBridge(label, serialNumber, model, manufacturer));
        return root;
    }

    public static BigInteger generateSalt() {
        return AutomatonUtils.generateSalt();
    }

    public static byte[] generateKey() throws InvalidAlgorithmParameterException {
        return AutomatonUtils.generateKey();
    }

    public static String generateMac() {
        return AutomatonUtils.generateMac();
    }

    public static class HomekitStandaloneAccessoryServer {
        private final HomekitRoot root;

        HomekitStandaloneAccessoryServer(Accessory accessory, NettyHttpServer webHandler, InetAddress localhost)
                throws UnknownHostException, IOException {
            root = new HomekitRoot(accessory.getLabel(), webHandler, localhost);
            root.addAccessory(accessory);
        }

        public void start() throws InterruptedException, ExecutionException {
            root.start();
        }
    }
}
