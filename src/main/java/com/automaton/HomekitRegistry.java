package com.automaton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.accessories.Accessory;
import com.automaton.characteristics.Characteristic;

public class HomekitRegistry {
    private static final Logger logger = LoggerFactory.getLogger(HomekitRegistry.class);

    private final Map<Integer, Accessory> accessories = new ConcurrentHashMap<>();
    private final Map<Accessory, List<AbstractAccessoryService>> services = new ConcurrentHashMap<>();
    private final Map<Accessory, Map<Integer, Characteristic>> characteristics = new ConcurrentHashMap<>();

    private final String label;

    private boolean isAllowUnauthenticatedRequests = false;

    public HomekitRegistry(String label) {
        this.label = label;
    }

    public synchronized void reset() {
        this.characteristics.clear();
        this.services.clear();
        for (Accessory accessory : this.accessories.values()) {
            List<AbstractAccessoryService> newServices = new ArrayList<>();
            try {
                newServices.add(new AccessoryServices.AccessoryInformationService(accessory));
                newServices.addAll(accessory.getServices());
            } catch (Exception e) {
                logger.error("Could not instantiate services for accessory " + accessory.getLabel(), e);
                this.services.put(accessory, Collections.emptyList());
                continue;
            }
            this.services.put(accessory, newServices);

            Map<Integer, Characteristic> newCharacteristics = new HashMap<>();
            int iid = 0;
            for (AbstractAccessoryService service : newServices) {
                iid++;
                for (Characteristic characteristic : service.getCharacteristics())
                    newCharacteristics.put(Integer.valueOf(++iid), characteristic);
            }
            this.characteristics.put(accessory, newCharacteristics);
        }
    }

    public String getLabel() {
        return this.label;
    }

    public Collection<Accessory> getAccessories() {
        return this.accessories.values();
    }

    public List<AbstractAccessoryService> getServices(Integer aid) {
        return Collections.unmodifiableList(this.services.get(this.accessories.get(aid)));
    }

    public Map<Integer, Characteristic> getCharacteristics(Integer aid) {
        Map<Integer, Characteristic> characteristics = this.characteristics.get(this.accessories.get(aid));
        if (characteristics == null)
            return Collections.emptyMap();
        return Collections.unmodifiableMap(characteristics);
    }

    public void add(Accessory accessory) {
        this.accessories.put(Integer.valueOf(accessory.getId()), accessory);
    }

    public void remove(Accessory accessory) {
        this.accessories.remove(Integer.valueOf(accessory.getId()));
    }

    public boolean isAllowUnauthenticatedRequests() {
        return this.isAllowUnauthenticatedRequests;
    }

    public void setAllowUnauthenticatedRequests(boolean allow) {
        this.isAllowUnauthenticatedRequests = allow;
    }
}
