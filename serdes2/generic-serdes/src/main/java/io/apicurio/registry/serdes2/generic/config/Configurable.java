package io.apicurio.registry.serdes2.generic.config;

import java.util.Map;

public interface Configurable {

    default void configure(Map<String, Object> rawConfigs) {
    }

    /**
     * TODO
     * Is this object ready to be used, e.g. both configured and enabled?
     *
     * @return
     */
    default boolean isReady() {
        return true;
    }
}
