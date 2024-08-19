package io.apicurio.registry.serdes2.generic.ids;

import io.apicurio.registry.resolver.strategy.ArtifactReference;
import io.apicurio.registry.serdes2.generic.SerDesException;
import io.apicurio.registry.serdes2.generic.config.IdOption;
import io.apicurio.registry.serdes2.generic.config.SerDeConfigReader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;

public class DefaultIdHandler implements IdHandler {
    static final int idSize = 8; // we use 8 / long

    private IdOption idOption = IdOption.GLOBAL_ID;

    @Override
    public void configure(Map<String, Object> rawConfig, boolean isKey) {
        var config = new SerDeConfigReader(rawConfig);
        idOption = config.useIdOption();
    }

    @Override
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

    @Override
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

    @Override
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
