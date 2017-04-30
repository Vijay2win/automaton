package com.automaton.server;

import java.io.*;
import java.math.BigInteger;
import java.util.Map.Entry;
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

    // marker to enable file system write.
    private final AtomicBoolean dirtyCache = new AtomicBoolean(false);
    private volatile File dataFile;

    public final String namespace;
    public final ConcurrentHashMap<String, byte[]> rows = new ConcurrentHashMap<>();

    public FileBasedDB(String namespace) {
        this.namespace = namespace;
        File namespaceDir = new File(new File(DB_FILE_LOCATION), namespace);
        if (!namespaceDir.exists())
            namespaceDir.mkdirs();
        this.dataFile = new File(namespaceDir, "data.db");
        load();
        EXECUTOR.scheduleAtFixedRate(new ScheduledSave(), 1, 1, TimeUnit.SECONDS);
    }

    public byte[] get(String key) {
        return rows.get(key);
    }

    /**
     * {@link putIfAbsent}
     */
    public String putIfAbsent(String key, String def) {
        byte[] bytes = putIfAbsent(key, def.getBytes(Charsets.UTF_8));
        return new String(bytes, Charsets.UTF_8);
    }

    /**
     * {@link putIfAbsent}
     */
    public BigInteger putIfAbsent(String key, BigInteger def) {
        byte[] bytes = putIfAbsent(key, def.toByteArray());
        return new BigInteger(bytes);
    }

    /**
     * {@link putIfAbsent}
     */
    public int putIfAbsent(String key, int def) {
        byte[] bytes = putIfAbsent(key, Ints.toByteArray(def));
        return Ints.fromByteArray(bytes);
    }

    /**
     * {@link putIfAbsent}
     */
    public long putIfAbsent(String key, long def) {
        byte[] bytes = putIfAbsent(key, Longs.toByteArray(def));
        return Longs.fromByteArray(bytes);
    }

    /**
     * Update forcefully, may also replace.
     */
    public void put(String key, byte[] value) {
        rows.put(key, value);
        dirtyCache.set(true);
    }

    public void put(String key, String value) {
        put(key, value.getBytes());
    }

    /**
     * A different implementation than regular put if absent. 
     * Will return an what's in the cache, if not adds and returns default. 
     */
    public byte[] putIfAbsent(String key, byte[] bytes) {
        byte[] b = rows.get(key);
        if (b != null)
            return b;

        put(key, bytes);
        return bytes;
    }

    public void remove(String key) {
        rows.remove(key);
        dirtyCache.set(true);
    }

    public int count() {
        return rows.size();
    }

    public void clear() {
        rows.clear();
    }

    public void load() {
        try {
            if (!dataFile.exists())
                return;

            try (DataInputStream stream = new DataInputStream(new FileInputStream(dataFile))) {
                while (stream.available() > 0) {
                    String key = stream.readUTF();
                    int size = stream.readInt();
                    byte[] bytes = new byte[size];
                    stream.readFully(bytes);
                    rows.put(key, bytes);
                }
            }

        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public class ScheduledSave implements Runnable {
        public void run() {
            try {
                // no new updates so skip.
                if (!dirtyCache.get())
                    return;

                String path = dataFile.getAbsolutePath();
                if (dataFile.exists())
                    dataFile.delete();

                // new file.
                dataFile = new File(path);
                try (DataOutputStream stream = new DataOutputStream(new FileOutputStream(dataFile))) {
                    for (Entry<String, byte[]> entry : rows.entrySet()) {
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
