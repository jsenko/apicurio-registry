package io.apicurio.registry.serdes2.generic.coordhandler;

import io.apicurio.registry.serdes2.generic.SerDesException;
import io.apicurio.registry.serdes2.generic.config.IdOption;
import io.apicurio.registry.serdes2.generic.coord.*;
import io.apicurio.registry.serdes2.generic.headers.DefaultHeadersHandlerConfigReader;
import io.apicurio.registry.serdes2.generic.record.GenericHeader;
import io.apicurio.registry.serdes2.generic.record.GenericHeaders;
import io.apicurio.registry.serdes2.generic.record.Record;
import io.apicurio.registry.serdes2.generic.record.meta.HeaderLikeMeta;
import io.apicurio.registry.serdes2.generic.record.meta.RecordMetadata;
import io.apicurio.registry.utils.IoUtil;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

public class DefaultHeadersCoordHandler implements RegistryContentCoordHandler {

    private String globalIdHeaderName;
    private String contentIdHeaderName;
    private String contentHashHeaderName;
    private String groupIdHeaderName;
    private String artifactIdHeaderName;
    private String versionHeaderName;
    private IdOption idOption;


    @Override
    public boolean isEnabled() {
        return false; // TODO
    }

    @Override
    public boolean isSupported(RecordMetadata recordMetadata) {
        return recordMetadata instanceof HeaderLikeMeta;
    }

//    @Override
//    public void writeCoord(Record<?> writeRecord, RegistryContentCoord coord) {
//
//    }

//    @Override
//    public Optional<RegistryContentCoord> readCoord(Record<?> readRecord) {
//        return Optional.empty();
//    }

    @Override
    public int idSize() {
        return 0;
    }

    @Override
    public void configure(Map<String, Object> rawConfigs) {

        var config = new DefaultHeadersHandlerConfigReader(rawConfigs);
        if (config.isKey()) {
            globalIdHeaderName = config.getKeyGlobalIdHeader();
            contentIdHeaderName = config.getKeyContentIdHeader();
            contentHashHeaderName = config.getKeyContentHashHeader();
            groupIdHeaderName = config.getKeyGroupIdHeader();
            artifactIdHeaderName = config.getKeyArtifactIdHeader();
            versionHeaderName = config.getKeyVersionHeader();
        } else {
            globalIdHeaderName = config.getValueGlobalIdHeader();
            contentIdHeaderName = config.getValueContentIdHeader();
            contentHashHeaderName = config.getValueContentHashHeader();
            groupIdHeaderName = config.getValueGroupIdHeader();
            artifactIdHeaderName = config.getValueArtifactIdHeader();
            versionHeaderName = config.getValueVersionHeader();
        }
        idOption = config.useIdOption();
    }


    @Override
    public Optional<RegistryContentCoord> readCoord(Record<?> readRecord) {
        var headers = ((HeaderLikeMeta) readRecord.metadata()).getHeaders();

        var globalId = getGlobalId(headers);
        var contentId = getContentId(headers);
        var contentHash = getContentHash(headers);
        var groupId = getGroupId(headers);
        var artifactId = getArtifactId(headers);
        var version = getVersion(headers);
        if (globalId != null) {
            return Optional.of(new GlobalIdCoord(globalId));
        } else if (contentId != null) {
            return Optional.of(new ContentIdCoord(contentId));
        } else if (contentHash != null) {
            return Optional.of(new ContentHashCoord(contentHash));
        } else if (groupId != null && artifactId != null && version != null) {
            // TODO Latest version?
            return Optional.of(new GAVCoord(groupId, artifactId, version));
        }
        // spotless:off
        /*
        return ArtifactReference.builder()
                .globalId(getGlobalId(headers))
                .contentId(getContentId(headers))
                .contentHash(getContentHash(headers))
                .groupId(getGroupId(headers))
                .artifactId(getArtifactId(headers))
                .version(getVersion(headers))
                .build();
         */
        // spotless:on
        return Optional.empty();
        //throw  new SerDesException("TODO");
    }


    @Override
    public void writeCoord(Record<?> writeRecord, RegistryContentCoord coord) {
        var headers = ((HeaderLikeMeta) writeRecord.metadata()).getHeaders();

        if (idOption == IdOption.CONTENT_ID) {
            if (!coord.isContentId()) {
                throw new SerDesException(
                        "Missing contentId. IdOption is contentId but there is no contentId in the ArtifactReference");
            }
            ByteBuffer buff = ByteBuffer.allocate(8);
            buff.putLong(((ContentIdCoord) coord).getContentId());
            headers.add(contentIdHeaderName, buff.array());
            return;
        }

        if (coord.isGlobalId()) {
            ByteBuffer buff = ByteBuffer.allocate(8);
            buff.putLong(((GlobalIdCoord) coord).getGlobalId());
            headers.add(globalIdHeaderName, buff.array());
        } else {
            if (coord.isContentHash()) {
                headers.add(contentHashHeaderName, IoUtil.toBytes(((ContentHashCoord) coord).getContentHash()));
            } else { // TODO Can we have both?

                headers.add(groupIdHeaderName, IoUtil.toBytes(((GAVCoord) coord).getGroupId()));
                headers.add(artifactIdHeaderName, IoUtil.toBytes(((GAVCoord) coord).getArtifactId()));
                // if (coord.getVersion() != null) { // TODO When can the version be null? Latest?
                headers.add(versionHeaderName, IoUtil.toBytes(((GAVCoord) coord).getVersion()));
            }
        }
    }


    private String getGroupId(GenericHeaders headers) {
        GenericHeader header = headers.lastHeader(groupIdHeaderName);
        if (header == null) {
            return null;
        }
        return IoUtil.toString(header.value());
    }

    private String getArtifactId(GenericHeaders headers) {
        GenericHeader header = headers.lastHeader(artifactIdHeaderName);
        if (header == null) {
            return null;
        }
        return IoUtil.toString(header.value());
    }

    private String getVersion(GenericHeaders headers) {
        GenericHeader header = headers.lastHeader(versionHeaderName);
        if (header == null) {
            return null;
        }
        return IoUtil.toString(header.value());
    }

    private Long getGlobalId(GenericHeaders headers) {
        GenericHeader header = headers.lastHeader(globalIdHeaderName);
        if (header == null) {
            return null;
        } else {
            return ByteBuffer.wrap(header.value()).getLong();
        }
    }

    private Long getContentId(GenericHeaders headers) {
        GenericHeader header = headers.lastHeader(contentIdHeaderName);
        if (header == null) {
            return null;
        } else {
            return ByteBuffer.wrap(header.value()).getLong();
        }
    }

    private String getContentHash(GenericHeaders headers) {
        GenericHeader header = headers.lastHeader(contentHashHeaderName);
        if (header == null) {
            return null;
        } else {
            return IoUtil.toString(header.value());
        }
    }

}
