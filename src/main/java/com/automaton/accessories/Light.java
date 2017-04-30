package com.automaton.accessories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.automaton.AbstractAccessoryService;
import com.automaton.AccessoryServices.LightService;
import com.automaton.characteristics.CharacteristicCallback;

/**
 * A simple light with a binary state.
 *
 * @author Andy Lintner
 */
public interface Light extends Accessory {

    /**
     * Retrieves the current binary state of the light.
     * 
     * @return a future that will contain the binary state
     */
    CompletableFuture<Boolean> getPowerState();

    /**
     * Sets the binary state of the light
     * 
     * @param powerState
     *            the binary state to set
     * @return a future that completes when the change is made
     * @throws Exception
     *             when the change cannot be made
     */
    CompletableFuture<Void> setPowerState(boolean powerState) throws Exception;

    @Override
    default Collection<AbstractAccessoryService> getServices() {
        return Collections.singleton(new LightService(this));
    }

    public interface ColorfulLight extends Light {

        /**
         * Retrieves the current hue of the light.
         * 
         * @return a future that will contain the hue, expressed in arc degrees from 0 to 360.
         */
        CompletableFuture<Double> getHue();

        /**
         * Sets the current hue of the light
         * 
         * @param value
         *            the hue to set, expressed in arc degrees from 0 to 360.
         * @return a future that completes when the hue is changed
         * @throws Exception
         *             when the hue cannot be changed.
         */
        CompletableFuture<Void> setHue(Double value) throws Exception;

        /**
         * Retrieves the saturation of the light.
         * 
         * @return a future that will contain the saturation, expressed as a value between 0 and 100.
         */
        CompletableFuture<Double> getSaturation();

        /**
         * Sets the saturation of the light.
         * 
         * @param value
         *            the saturation to set, expressed as a value between 0 and 100.
         * @return a future that completes when the saturation is changed.
         * @throws Exception
         *             when the saturation cannot be set.
         */
        CompletableFuture<Void> setSaturation(Double value) throws Exception;

        /**
         * Subscribes to changes in the saturation of the light.
         * 
         * @param callback
         *            the function to call when the state changes.
         */
        void subscribeSaturation(CharacteristicCallback callback);

        /**
         * Unsubscribes from changes in the saturation of the light.
         */
        void unsubscribeSaturation();
    }

    public interface DimmableLight extends Light {
        /**
         * Retrieves the current brightness of the light
         * 
         * @return a future that will contain the brightness, expressed as an integer between 0 and 100.
         */
        CompletableFuture<Integer> getBrightness();

        /**
         * Sets the current brightness of the light
         * 
         * @param value
         *            the brightness, on a scale of 0 to 100, to set
         * @return a future that completes when the brightness is changed
         * @throws Exception
         *             when the brightness cannot be set
         */
        CompletableFuture<Void> setBrightness(Integer value) throws Exception;
    }
}
