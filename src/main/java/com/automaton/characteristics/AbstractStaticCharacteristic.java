package com.automaton.characteristics;

import java.util.concurrent.CompletableFuture;

import javax.json.*;

import com.automaton.accessories.Accessory;

public abstract class AbstractStaticCharacteristic extends AbstractCharacteristic<String> {
    private static final int MAX_LEN = 255;
    private final String value;

    public AbstractStaticCharacteristic(String type, String description, String value) {
        super(type, "string", false, true, description);
        this.value = value;
    }

    protected CompletableFuture<JsonObjectBuilder> makeBuilder(int iid) {
        return super.makeBuilder(iid).thenApply(builder -> builder.add("maxLen", 255));
    }

    public String convert(JsonValue jsonValue) {
        return ((JsonString) jsonValue).getString();
    }

    public void setValue(String value) throws Exception {
        throw new Exception("Cannot modify static strings");
    }

    protected CompletableFuture<String> getValue() {
        return CompletableFuture.completedFuture(this.value).thenApply(s -> (s != null) ? s : "Unavailable");
    }

    protected String getDefault() {
        return "Unknown";
    }

    public static class Manufacturer extends AbstractStaticCharacteristic {
        public Manufacturer(Accessory accessory) throws Exception {
            super("00000020-0000-1000-8000-0026BB765291", "The name of the manufacturer", accessory.getManufacturer());
        }
    }

    public static class Model extends AbstractStaticCharacteristic {
        public Model(Accessory accessory) throws Exception {
            super("00000021-0000-1000-8000-0026BB765291", "The name of the model", accessory.getModel());
        }
    }

    public static class Name extends AbstractStaticCharacteristic {
        public Name(String label) {
            super("00000023-0000-1000-8000-0026BB765291", "Name of the accessory", label);
        }
    }

    public static class SerialNumber extends AbstractStaticCharacteristic {
        public SerialNumber(Accessory accessory) throws Exception {
            super("00000030-0000-1000-8000-0026BB765291", "The serial number of the accessory",
                    accessory.getSerialNumber());
        }
    }
}
