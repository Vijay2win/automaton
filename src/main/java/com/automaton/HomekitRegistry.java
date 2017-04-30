package com.automaton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.AccessoryServices.AccessoryInformationService;
import com.automaton.accessories.Accessory;
import com.automaton.characteristics.Characteristic;

public class HomekitRegistry {
    private final static Logger logger = LoggerFactory.getLogger(HomekitRegistry.class);

    private final Map<Integer, Accessory> accessories = new ConcurrentHashMap<>();
    private final Map<Accessory, List<AbstractAccessoryService>> services = new ConcurrentHashMap<>();
    private final Map<Accessory, Map<Integer, Characteristic>> characteristics = new ConcurrentHashMap<>();

    private final String label;
    private boolean isAllowUnauthenticatedRequests = false;

    public HomekitRegistry(String label) {
        this.label = label;
    }

    public synchronized void reset() {
        characteristics.clear();
        services.clear();
        for (Accessory accessory : accessories.values()) {
            List<AbstractAccessoryService> newServices = new ArrayList<>();
            try {
                newServices.add(new AccessoryInformationService(accessory));
                newServices.addAll(accessory.getServices());
            } catch (Exception e) {
                logger.error("Could not instantiate services for accessory " + accessory.getLabel(), e);
                services.put(accessory, Collections.emptyList());
                continue;
            }
            services.put(accessory, newServices);

            Map<Integer, Characteristic> newCharacteristics = new HashMap<>();
            int iid = 0;
            for (AbstractAccessoryService service : newServices) {
                iid++;
                for (Characteristic characteristic : service.getCharacteristics())
                    newCharacteristics.put(++iid, characteristic);
            }
            characteristics.put(accessory, newCharacteristics);
        }
    }

    public String getLabel() {
        return label;
    }

    public Collection<Accessory> getAccessories() {
        return accessories.values();
    }

    public List<AbstractAccessoryService> getServices(Integer aid) {
        return Collections.unmodifiableList(services.get(accessories.get(aid)));
    }

    public Map<Integer, Characteristic> getCharacteristics(Integer aid) {
        Map<Integer, Characteristic> characteristics = this.characteristics.get(accessories.get(aid));
        if (characteristics == null)
            return Collections.emptyMap();
        return Collections.unmodifiableMap(characteristics);
    }

    public void add(Accessory accessory) {
        accessories.put(accessory.getId(), accessory);
    }

    public void remove(Accessory accessory) {
        accessories.remove(accessory.getId());
    }

    public boolean isAllowUnauthenticatedRequests() {
        return isAllowUnauthenticatedRequests;
    }

    public void setAllowUnauthenticatedRequests(boolean allow) {
        this.isAllowUnauthenticatedRequests = allow;
    }
}
