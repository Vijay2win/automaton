package com.automaton.http;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import javax.json.*;

import com.automaton.AbstractAccessoryService;
import com.automaton.HomekitRegistry;
import com.automaton.accessories.Accessory;
import com.automaton.characteristics.Characteristic;
import com.automaton.http.HttpResponses.HapJsonResponse;

public class AccessoryController {
    public AccessoryController(HomekitRegistry registry) {
        this.registry = registry;
    }

    private final HomekitRegistry registry;

    public HttpResponse listing() throws Exception {
        JsonArrayBuilder accessories = Json.createArrayBuilder();

        Map<Integer, List<CompletableFuture<JsonObject>>> accessoryServiceFutures = new HashMap<>();
        for (Accessory accessory : registry.getAccessories()) {
            int iid = 0;
            List<CompletableFuture<JsonObject>> serviceFutures = new ArrayList<>();
            for (AbstractAccessoryService service : registry.getServices(Integer.valueOf(accessory.getId()))) {
                serviceFutures.add(toJson(service, iid));
                iid += service.getCharacteristics().size() + 1;
            }
            accessoryServiceFutures.put(accessory.getId(), serviceFutures);
        }

        Map<Integer, JsonArrayBuilder> serviceArrayBuilders = new HashMap<>();
        for (Map.Entry<Integer, List<CompletableFuture<JsonObject>>> entry : accessoryServiceFutures.entrySet()) {
            JsonArrayBuilder arr = Json.createArrayBuilder();
            for (CompletableFuture<JsonObject> future : entry.getValue())
                arr.add((JsonValue) future.join());
            serviceArrayBuilders.put(entry.getKey(), arr);
        }

        for (Accessory accessory : registry.getAccessories()) {
            accessories.add(Json.createObjectBuilder().add("aid", accessory.getId()).add("services",
                    serviceArrayBuilders.get(accessory.getId())));
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Json.createWriter(baos)
                    .write(Json.createObjectBuilder().add("accessories", accessories).build());
            return new HapJsonResponse(baos.toByteArray());
        }
    }

    private CompletableFuture<JsonObject> toJson(AbstractAccessoryService service, int interfaceId) throws Exception {
        JsonObjectBuilder builder = Json.createObjectBuilder().add("iid", ++interfaceId).add("type", service.getType());
        List<Characteristic> characteristics = service.getCharacteristics();
        Collection<CompletableFuture<JsonObject>> characteristicFutures = new ArrayList<>(characteristics.size());
        for (Characteristic characteristic : characteristics) {
            characteristicFutures.add(characteristic.toJson(++interfaceId));
        }
        return CompletableFuture.allOf((CompletableFuture<?>[]) characteristicFutures
                .toArray(new CompletableFuture[characteristicFutures.size()])).thenApply(v -> {
                    JsonArrayBuilder jsonCharacteristics = Json.createArrayBuilder();
                    characteristicFutures.stream().map(future -> future.join())
                            .forEach(c -> jsonCharacteristics.add(c));
                    builder.add("characteristics", jsonCharacteristics);
                    return builder.build();
                });
    }
}
