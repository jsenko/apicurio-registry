package io.apicurio.registry.serdes2.generic;

import io.apicurio.registry.resolver.SchemaResolver;
import io.apicurio.registry.resolver.SchemaResolverConfig;
import io.apicurio.registry.serde.IdHandler;
import io.apicurio.registry.serde.Legacy4ByteIdHandler;
import io.apicurio.registry.serde.SerdeConfig;
import io.apicurio.registry.serde.headers.HeadersHandler;
import io.apicurio.registry.serdes2.generic.config.Configurable;
import io.apicurio.registry.serdes2.generic.config.SerDeConfigReader;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Objects;

import static io.apicurio.registry.serde.SerdeConfig.*;

public abstract class GenericSerDe<SCHEMA, DATA> implements Configurable, Closeable {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public static final byte MAGIC_BYTE = 0x0;

    @Getter
    @Setter
    protected IdHandler idHandler;

    @Getter
    @Setter
    protected HeadersHandler headersHandler;

    @Getter
    @Setter
    @NonNull // For the setter
    protected SchemaResolver<SCHEMA, DATA> schemaResolver;

    protected GenericSerDeDatatype<SCHEMA, DATA> serDeDatatype;

    protected SerDeConfigReader serDeConfig;

    public GenericSerDe(GenericSerDeDatatype<SCHEMA, DATA> serDeDatatype,
                        SchemaResolver<SCHEMA, DATA> schemaResolver) {
        this.serDeDatatype = serDeDatatype;
        this.schemaResolver = schemaResolver;
    }

    @Override
    public void configure(Map<String, Object> rawConfigs) {
        Objects.requireNonNull(rawConfigs);
        serDeConfig = new SerDeConfigReader(rawConfigs);

        if (schemaResolver == null) {
            if (!serDeConfig.hasConfig(SCHEMA_RESOLVER)) {
                serDeConfig.setConfig(SCHEMA_RESOLVER, SCHEMA_RESOLVER_DEFAULT);
            }
            schemaResolver = (SchemaResolver<SCHEMA, DATA>) serDeConfig.getSchemaResolver();
        }

        // enforce default artifactResolverStrategy for kafka apps // TODO
        if (!serDeConfig.hasConfig(SchemaResolverConfig.ARTIFACT_RESOLVER_STRATEGY)) {
            serDeConfig.setConfig(SchemaResolverConfig.ARTIFACT_RESOLVER_STRATEGY,
                    SerdeConfig.ARTIFACT_RESOLVER_STRATEGY_DEFAULT);
        }

        schemaResolver.configure(serDeConfig.getRawConfig(), serDeDatatype.getSchemaParser());

        if (serDeConfig.enableConfluentIdHandler()) {
            if (idHandler != null && !(idHandler instanceof Legacy4ByteIdHandler)) {
                log.warn(
                        "Conflicting ID handler configuration. Using Legacy4ByteIdHandler (Confluent ID handler).");
            }
            idHandler = new Legacy4ByteIdHandler();
        } else {
            if (idHandler == null) {
                if (!serDeConfig.hasConfig(ID_HANDLER)) {
                    serDeConfig.setConfig(ID_HANDLER, ID_HANDLER_DEFAULT);
                }
                idHandler = serDeConfig.getIDHandler();
            }
        }
        idHandler.configure(serDeConfig.getRawConfig(), serDeConfig.isKey());

        if (serDeConfig.enableHeaders()) {
            headersHandler = serDeConfig.getHeadersHandler();
            headersHandler.configure(serDeConfig.getRawConfig(), serDeConfig.isKey());
        }
    }

    @Override
    public void close() {
        try {
            schemaResolver.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
