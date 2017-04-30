package com.automaton.http;

import java.io.ByteArrayOutputStream;

import javax.json.*;

import com.automaton.characteristics.EventableCharacteristic;
import com.automaton.http.HttpResponses.EventResponse;

public class EventController {

    public HttpResponse getMessage(int accessoryId, int iid, EventableCharacteristic changed) throws Exception {
        JsonArrayBuilder characteristics = Json.createArrayBuilder();

        JsonObjectBuilder characteristicBuilder = Json.createObjectBuilder();
        characteristicBuilder.add("aid", accessoryId);
        characteristicBuilder.add("iid", iid);
        changed.supplyValue(characteristicBuilder);
        characteristics.add(characteristicBuilder.build());

        JsonObject data = Json.createObjectBuilder().add("characteristics", characteristics).build();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Json.createWriter(baos).write(data);
            return new EventResponse(baos.toByteArray());
        }
    }
}
