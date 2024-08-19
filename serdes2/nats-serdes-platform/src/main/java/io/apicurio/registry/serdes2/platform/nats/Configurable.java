package io.apicurio.registry.serdes2.platform.nats;

import java.util.Map;

public interface Configurable {

    void configure(Map<String, Object> configs);
}
