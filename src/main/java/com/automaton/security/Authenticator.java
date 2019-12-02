package com.automaton.security;

import java.io.IOError;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitServer;
import com.automaton.server.FileBasedDB;

public class Authenticator {
    private static final Logger logger = LoggerFactory.getLogger(Authenticator.class);

    private static final String PIN = "000-00-001";
    private static final FileBasedDB AUTH_INFO_DB = new FileBasedDB("auth-info");
    public static final Authenticator INSTANCE = new Authenticator();

    private final String mac;
    private final BigInteger salt;
    private final byte[] privateKey;

    public Authenticator() {
        try {
            this.mac = AUTH_INFO_DB.putIfAbsent("mac-address", HomekitServer.generateMac());
            this.salt = AUTH_INFO_DB.putIfAbsent("mac-address", HomekitServer.generateSalt());
            this.privateKey = AUTH_INFO_DB.putIfAbsent("mac-address", HomekitServer.generateKey());
        } catch (Throwable th) {
            throw new IOError(th);
        }
        logger.info("The PIN for pairing is {}", "000-00-001");
    }

    public String getPin() {
        return "000-00-001";
    }

    public String getMac() {
        return this.mac;
    }

    public BigInteger getSalt() {
        return this.salt;
    }

    public byte[] getPrivateKey() {
        return this.privateKey;
    }

    public void createUser(String username, byte[] publicKey) {
        AUTH_INFO_DB.put(getMac() + username, publicKey);
        logger.info("Added pairing for {}", username);
    }

    public void removeUser(String username) {
        AUTH_INFO_DB.remove(getMac() + username);
        logger.info("Removed pairing for {}", username);
    }

    public byte[] getUserPublicKey(String username) {
        return AUTH_INFO_DB.get(getMac() + username);
    }
}
