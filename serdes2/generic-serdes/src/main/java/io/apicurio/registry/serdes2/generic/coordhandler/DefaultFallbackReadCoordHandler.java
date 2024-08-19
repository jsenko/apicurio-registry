package io.apicurio.registry.serdes2.generic.coordhandler;

import io.apicurio.registry.serde.SerdeConfig;
import io.apicurio.registry.serdes2.generic.coord.GAVCoord;
import io.apicurio.registry.serdes2.generic.coord.RegistryContentCoord;
import io.apicurio.registry.serdes2.generic.record.Record;
import io.apicurio.registry.serdes2.generic.record.meta.RecordMetadata;

import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of FallbackArtifactProvider that simply uses config properties
 */
public class DefaultFallbackReadCoordHandler implements RegistryContentReadCoordHandler {

    private GAVCoord fallbackArtifactReference;


    @Override
    public void configure(Map<String, Object> configs) {
        // TODO Create config reader for this class
        var isKey = false; // TODO

        String groupIdConfigKey = SerdeConfig.FALLBACK_ARTIFACT_GROUP_ID;
        if (isKey) {
            groupIdConfigKey += ".key";
        }
        String fallbackGroupId = (String) configs.get(groupIdConfigKey);

        String artifactIdConfigKey = SerdeConfig.FALLBACK_ARTIFACT_ID;
        if (isKey) {
            artifactIdConfigKey += ".key";
        }
        String fallbackArtifactId = (String) configs.get(artifactIdConfigKey);

        String versionConfigKey = SerdeConfig.FALLBACK_ARTIFACT_VERSION;
        if (isKey) {
            versionConfigKey += ".key";
        }
        String fallbackVersion = (String) configs.get(versionConfigKey);

        if (fallbackArtifactId != null) {
            fallbackArtifactReference = new GAVCoord(fallbackGroupId, fallbackArtifactId, fallbackVersion);
        }

    }

    @Override
    public boolean isEnabled() {
        return fallbackArtifactReference != null;
    }

    @Override
    public boolean isSupported(RecordMetadata recordMetadata) {
        return true;
    }

    @Override
    public Optional<RegistryContentCoord> readCoord(Record<?> readRecord) {
        return Optional.ofNullable(fallbackArtifactReference);
    }

    @Override
    public int idSize() {
        return 0;
    }
}
