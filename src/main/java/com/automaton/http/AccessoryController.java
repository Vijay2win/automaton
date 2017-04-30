package com.automaton.http;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.automaton.AbstractAccessoryService;
import com.automaton.HomekitRegistry;
import com.automaton.accessories.Accessory;
import com.automaton.characteristics.Characteristic;
import com.automaton.http.HttpResponses.HapJsonResponse;
import com.google.common.util.concurrent.Futures;

public class AccessoryController {
    private final HomekitRegistry registry;

    public AccessoryController(HomekitRegistry registry) {
        this.registry = registry;
    }

    public HttpResponse listing() throws Exception {
        Map<Integer, List<CompletableFuture<JsonObject>>> accessoryServices = new HashMap<>();
        for (Accessory accessory : registry.getAccessories()) {
            int iid = 0;
            List<CompletableFuture<JsonObject>> serviceFutures = new ArrayList<>();
            for (AbstractAccessoryService service : registry.getServices(accessory.getId())) {
                serviceFutures.add(toJson(service, iid));
                iid += service.getCharacteristics().size() + 1;
            }
            accessoryServices.put(accessory.getId(), serviceFutures);
        }

        Map<Integer, JsonArrayBuilder> serviceArrayBuilders = new HashMap<>();
        accessoryServices.entrySet().forEach(entry -> {
            JsonArrayBuilder services = Json.createArrayBuilder();
            entry.getValue().forEach(future -> services.add(Futures.getUnchecked(future)));
            serviceArrayBuilders.put(entry.getKey(), services);
        });

        JsonArrayBuilder accessories = Json.createArrayBuilder();
        registry.getAccessories().forEach(accessory -> accessories.add(Json.createObjectBuilder()
                .add("aid", accessory.getId())
                .add("services", serviceArrayBuilders.get(accessory.getId()))));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Json.createWriter(baos).write(Json.createObjectBuilder().add("accessories", accessories).build());
            return new HapJsonResponse(baos.toByteArray());
        }
    }

    private static CompletableFuture<JsonObject> toJson(AbstractAccessoryService service, int interfaceId) throws Exception {
        JsonObjectBuilder builder = Json.createObjectBuilder().add("iid", ++interfaceId).add("type", service.getType());
        List<Characteristic> characteristics = service.getCharacteristics();
        Collection<CompletableFuture<JsonObject>> furures = new ArrayList<>(characteristics.size());
        for (Characteristic characteristic : characteristics)
            furures.add(characteristic.toJson(++interfaceId));

        return CompletableFuture.allOf(furures.toArray(new CompletableFuture<?>[furures.size()])).thenApply(v -> {
            JsonArrayBuilder json = Json.createArrayBuilder();
            furures.stream().map(future -> future.join()).forEach(c -> json.add(c));
            return builder.add("characteristics", json).build();
        });
    }
}
