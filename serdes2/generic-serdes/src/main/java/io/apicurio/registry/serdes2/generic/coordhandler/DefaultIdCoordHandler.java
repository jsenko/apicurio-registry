package io.apicurio.registry.serdes2.generic.coordhandler;

import io.apicurio.registry.resolver.strategy.ArtifactReference;
import io.apicurio.registry.serdes2.generic.SerDesException;
import io.apicurio.registry.serdes2.generic.config.IdOption;
import io.apicurio.registry.serdes2.generic.config.SerDeConfigReader;
import io.apicurio.registry.serdes2.generic.coord.ContentIdCoord;
import io.apicurio.registry.serdes2.generic.coord.GlobalIdCoord;
import io.apicurio.registry.serdes2.generic.coord.RegistryContentCoord;
import io.apicurio.registry.serdes2.generic.record.Record;
import io.apicurio.registry.serdes2.generic.record.meta.RecordMetadata;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

public class DefaultIdCoordHandler implements RegistryContentCoordHandler {
    static final int idSize = 8; // we use 8 / long

    private IdOption idOption = IdOption.GLOBAL_ID;

    @Override
    public void configure(Map<String, Object> rawConfig) {
        var config = new SerDeConfigReader(rawConfig);
        idOption = config.useIdOption();
    }


    @Override
    public boolean isEnabled() {
        return true; // TODO?
    }

    @Override
    public boolean isSupported(RecordMetadata recordMetadata) {
        // TODO Is supported on coord? Have multiple coords available at the same time?
        return true;
    }


    @Override
    public Optional<RegistryContentCoord> readCoord(Record<?> readRecord) {
        return Optional.empty();
    }


    @Override
    public void writeCoord(Record<?> writeRecord, RegistryContentCoord coord) {
        var buffer = writeRecord.rawPayload();

        long id;
        if (idOption == IdOption.CONTENT_ID) {
            if (!coord.isContentId()) {
                throw new SerDesException(
                        "Missing contentId. IdOption is contentId but there is no contentId in the ArtifactReference");
            }
            id = ((ContentIdCoord) coord).getContentId();
        } else {
            // TODO Check
            id = ((GlobalIdCoord) coord).getGlobalId();
        }
        buffer.putLong(id);
    }


    //@Override
    public void writeId(ArtifactReference reference, OutputStream out) throws IOException {
        long id;
        if (idOption == IdOption.CONTENT_ID) {
            if (reference.getContentId() == null) {
                throw new SerDesException(
                        "Missing contentId. IdOption is contentId but there is no contentId in the ArtifactReference");
            }
            id = reference.getContentId();
        } else {
            id = reference.getGlobalId();
        }
        out.write(ByteBuffer.allocate(idSize).putLong(id).array());
    }

    //@Override
    public void writeId(ArtifactReference reference, ByteBuffer buffer) {
        long id;
        if (idOption == IdOption.CONTENT_ID) {
            if (reference.getContentId() == null) {
                throw new SerDesException(
                        "Missing contentId. IdOption is contentId but there is no contentId in the ArtifactReference");
            }
            id = reference.getContentId();
        } else {
            id = reference.getGlobalId();
        }
        buffer.putLong(id);
    }

    //@Override
    public ArtifactReference readId(ByteBuffer buffer) {
        if (idOption == IdOption.CONTENT_ID) {
            return ArtifactReference.builder().contentId(buffer.getLong()).build();
        } else {
            return ArtifactReference.builder().globalId(buffer.getLong()).build();
        }
    }

    @Override
    public int idSize() {
        return idSize;
    }
}
