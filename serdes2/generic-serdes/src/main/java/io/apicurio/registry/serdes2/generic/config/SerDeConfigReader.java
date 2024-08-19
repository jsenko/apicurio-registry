package io.apicurio.registry.serdes2.generic.config;

import io.apicurio.registry.resolver.SchemaResolver;
import io.apicurio.registry.serde.fallback.FallbackArtifactProvider;
import io.apicurio.registry.serdes2.generic.headers.HeadersHandler;
import io.apicurio.registry.serdes2.generic.ids.IdHandler;

import java.util.Map;

import static io.apicurio.registry.serdes2.generic.config.SerDeConfig.*;

public class SerDeConfigReader extends GenericConfigReader {

    public SerDeConfigReader(Map<String, Object> rawConfig) {
        super(rawConfig);
    }

    public SerDeConfigReader(GenericConfigReader config) {
        super(config.getRawConfig());
    }

    public SchemaResolver getSchemaResolver() {
        return getInstance(SCHEMA_RESOLVER, SchemaResolver.class);
    }

    public boolean enableConfluentIdHandler() {
        return getBooleanOrDefault(ENABLE_CONFLUENT_ID_HANDLER, false);
    }

    public boolean isKey() {
        return getBooleanOrDefault(IS_KEY, false);
    }

    public boolean enableHeaders() {
        return getBooleanOrDefault(ENABLE_HEADERS, false);
    }

    public HeadersHandler getHeadersHandler() {
        return getInstance(HEADERS_HANDLER, HeadersHandler.class);
    }

    public IdHandler getIDHandler() {
        return getInstance(ID_HANDLER, IdHandler.class);
    }

    public FallbackArtifactProvider getFallbackArtifactProvider() {
        return getInstance(FALLBACK_ARTIFACT_PROVIDER, FallbackArtifactProvider.class);
    }

    public IdOption useIdOption() {
        return IdOption.of(getString(USE_ID));
    }
}
