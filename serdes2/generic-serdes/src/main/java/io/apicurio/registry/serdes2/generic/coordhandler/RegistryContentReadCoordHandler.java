package io.apicurio.registry.serdes2.generic.coordhandler;

import io.apicurio.registry.serdes2.generic.config.Configurable;
import io.apicurio.registry.serdes2.generic.coord.RegistryContentCoord;
import io.apicurio.registry.serdes2.generic.record.Record;
import io.apicurio.registry.serdes2.generic.record.meta.RecordMetadata;

import java.util.Optional;

public interface RegistryContentReadCoordHandler extends Configurable {

    boolean isEnabled();

    boolean isSupported(RecordMetadata recordMetadata);

    Optional<RegistryContentCoord> readCoord(Record<?> readRecord);

    int idSize();
}
