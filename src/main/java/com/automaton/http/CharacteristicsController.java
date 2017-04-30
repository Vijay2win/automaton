package com.automaton.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.HomekitRegistry;
import com.automaton.characteristics.Characteristic;
import com.automaton.characteristics.EventableCharacteristic;
import com.automaton.http.HttpResponses.HapJsonNoContentResponse;
import com.automaton.http.HttpResponses.HapJsonResponse;
import com.automaton.http.HttpResponses.NotFoundResponse;
import com.automaton.server.SubscriptionManager;
import com.automaton.utils.AutomatonUtils;

import io.netty.handler.codec.http.FullHttpRequest;

public class CharacteristicsController {
    private static final Logger logger = LoggerFactory.getLogger(CharacteristicsController.class);

    private final HomekitRegistry registry;
    private final SubscriptionManager subscriptions;

    public CharacteristicsController(HomekitRegistry registry, SubscriptionManager subscriptions) {
        this.registry = registry;
        this.subscriptions = subscriptions;
    }

    public HttpResponse get(FullHttpRequest request) throws Exception {
        String uri = request.getUri();
        // Characteristics are requested with /characteristics?id=1.1,2.1,3.1
        String query = uri.substring("/characteristics?id=".length());
        String[] ids = query.split(",");
        JsonArrayBuilder characteristics = Json.createArrayBuilder();
        for (String id : ids) {
            String[] parts = id.split("\\.");
            if (parts.length != 2) {
                logger.error("Unexpected characteristics request: " + uri);
                return new NotFoundResponse();
            }
            int aid = Integer.parseInt(parts[0]);
            int iid = Integer.parseInt(parts[1]);
            JsonObjectBuilder characteristic = Json.createObjectBuilder();
            Map<Integer, Characteristic> characteristicMap = registry.getCharacteristics(aid);
            if (!characteristicMap.isEmpty()) {
                Characteristic targetCharacteristic = characteristicMap.get(iid);
                if (targetCharacteristic != null) {
                    targetCharacteristic.supplyValue(characteristic);

                    characteristics.add(characteristic.add("aid", aid).add("iid", iid).build());
                } else {
                    logger.warn("Accessory " + aid + " does not have characteristic " + iid + "Request: " + uri);
                }
            } else {
                logger.warn("Accessory " + aid + " has no characteristics or does not exist. Request: " + uri);
            }
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Json.createWriter(baos).write(Json.createObjectBuilder().add("characteristics", characteristics.build()).build());
            return new HapJsonResponse(baos.toByteArray());
        }
    }

    public HttpResponse put(FullHttpRequest request, HomekitConnection connection) throws Exception {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(AutomatonUtils.readAllRemaining(request.content()))) {
            JsonArray jsonCharacteristics = Json.createReader(bais).readObject().getJsonArray("characteristics");
            for (JsonValue value : jsonCharacteristics) {
                JsonObject jsonCharacteristic = (JsonObject) value;
                int aid = jsonCharacteristic.getInt("aid");
                int iid = jsonCharacteristic.getInt("iid");
                Characteristic characteristic = registry.getCharacteristics(aid).get(iid);

                if (jsonCharacteristic.containsKey("value")) {
                    characteristic.setValue(jsonCharacteristic.get("value"));
                }
                if (jsonCharacteristic.containsKey("ev") && characteristic instanceof EventableCharacteristic) {
                    if (jsonCharacteristic.getBoolean("ev")) {
                        subscriptions.addSubscription(aid, iid, (EventableCharacteristic) characteristic, connection);
                    } else {
                        subscriptions.removeSubscription((EventableCharacteristic) characteristic, connection);
                    }
                }
            }
        }
        return new HapJsonNoContentResponse();
    }
}
