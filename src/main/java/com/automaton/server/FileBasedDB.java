package com.automaton.server;

import java.io.*;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class FileBasedDB {
    private static final Logger logger = LoggerFactory.getLogger(FileBasedDB.class);

    public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);
    public static final String DB_FILE_LOCATION = AutomatonConfiguration.getString("db.file.location", "/tmp/vj_hub");

    private final AtomicBoolean dirtyCache = new AtomicBoolean(false);

    private volatile File dataFile;
    public final String namespace;
    public final ConcurrentHashMap<String, byte[]> rows = (ConcurrentHashMap) new ConcurrentHashMap<>();

    public FileBasedDB(String namespace) {
        this.namespace = namespace;
        File namespaceDir = new File(new File(DB_FILE_LOCATION), namespace);
        if (!namespaceDir.exists())
            namespaceDir.mkdirs();
        this.dataFile = new File(namespaceDir, "data.db");
        load();
        EXECUTOR.scheduleAtFixedRate(new ScheduledSave(), 1L, 1L, TimeUnit.SECONDS);
    }

    public byte[] get(String key) {
        return this.rows.get(key);
    }

    public String putIfAbsent(String key, String def) {
        byte[] bytes = putIfAbsent(key, def.getBytes(Charsets.UTF_8));
        return new String(bytes, Charsets.UTF_8);
    }

    public BigInteger putIfAbsent(String key, BigInteger def) {
        byte[] bytes = putIfAbsent(key, def.toByteArray());
        return new BigInteger(bytes);
    }

    public int putIfAbsent(String key, int def) {
        byte[] bytes = putIfAbsent(key, Ints.toByteArray(def));
        return Ints.fromByteArray(bytes);
    }

    public long putIfAbsent(String key, long def) {
        byte[] bytes = putIfAbsent(key, Longs.toByteArray(def));
        return Longs.fromByteArray(bytes);
    }

    public void put(String key, byte[] value) {
        this.rows.put(key, value);
        this.dirtyCache.set(true);
    }

    public void put(String key, String value) {
        put(key, value.getBytes());
    }

    public byte[] putIfAbsent(String key, byte[] bytes) {
        byte[] b = this.rows.get(key);
        if (b != null) {
            return b;
        }
        put(key, bytes);
        return bytes;
    }

    public void remove(String key) {
        this.rows.remove(key);
        this.dirtyCache.set(true);
    }

    public int count() {
        return this.rows.size();
    }

    public void clear() {
        this.rows.clear();
    }

    public void load() {
        try {
            if (!this.dataFile.exists()) {
                return;
            }
            try (DataInputStream stream = new DataInputStream(new FileInputStream(this.dataFile))) {
                while (stream.available() > 0) {
                    String key = stream.readUTF();
                    int size = stream.readInt();
                    byte[] bytes = new byte[size];
                    stream.readFully(bytes);
                    this.rows.put(key, bytes);
                }

            }
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public class ScheduledSave implements Runnable {
        public void run() {
            try {
                if (!FileBasedDB.this.dirtyCache.get()) {
                    return;
                }
                String path = FileBasedDB.this.dataFile.getAbsolutePath();
                if (FileBasedDB.this.dataFile.exists()) {
                    FileBasedDB.this.dataFile.delete();
                }

                FileBasedDB.this.dataFile = new File(path);
                try (DataOutputStream stream = new DataOutputStream(new FileOutputStream(FileBasedDB.this.dataFile))) {
                    for (Map.Entry<String, byte[]> entry : FileBasedDB.this.rows.entrySet()) {
                        stream.writeUTF(entry.getKey());
                        byte[] value = entry.getValue();
                        stream.writeInt(value.length);
                        stream.write(value);
                    }
                }
            } catch (Throwable th) {
                logger.error("Exception in saving to file", th);
            }
        }
    }
}
