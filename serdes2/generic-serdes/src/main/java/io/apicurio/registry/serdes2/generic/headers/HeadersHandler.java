package io.apicurio.registry.serdes2.generic.headers;

import io.apicurio.registry.serdes2.generic.config.Configurable;
import io.apicurio.registry.serdes2.generic.coord.RegistryContentCoord;
import io.apicurio.registry.serdes2.generic.record.GenericHeaders;

/**
 * Common interface for headers handling when serializing/deserializing kafka records that have
 * {@link Headers}
 */
public interface HeadersHandler extends Configurable {

    String HEADER_KEY_ENCODING = "apicurio.key.encoding"; // TODO
    String HEADER_VALUE_ENCODING = "apicurio.value.encoding"; // TODO

    /**
     * Reads the kafka message headers and returns an ArtifactReference that can contain or not information to
     * identify an Artifact in the registry.
     *
     * @param headers
     * @return ArtifactReference
     */
    RegistryContentCoord readHeaders(GenericHeaders headers);

    void writeHeaders(GenericHeaders headers, RegistryContentCoord reference);
}
