package com.automaton.characteristics;

import java.util.concurrent.CompletableFuture;

import javax.json.*;

public interface Characteristic {
    void supplyValue(JsonObjectBuilder paramJsonObjectBuilder);

    CompletableFuture<JsonObject> toJson(int paramInt);

    void setValue(JsonValue paramJsonValue);
}
