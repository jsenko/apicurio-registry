package io.apicurio.registry.serdes2.generic.headers;

import io.apicurio.registry.serdes2.generic.config.SerDeConfigReader;

import java.util.Map;

import static io.apicurio.registry.serdes2.generic.config.SerDeConfig.*;

public class DefaultHeadersHandlerConfigReader extends SerDeConfigReader {


    public DefaultHeadersHandlerConfigReader(Map<String, Object> rawConfig) {
        super(rawConfig);
    }

    public String getKeyGlobalIdHeader() {
        return getStringOrDefault(HEADER_KEY_GLOBAL_ID_OVERRIDE_NAME, HEADER_KEY_GLOBAL_ID_OVERRIDE_NAME_DEFAULT);
    }

    public String getKeyContentIdHeader() {
        return getStringOrDefault(HEADER_KEY_CONTENT_ID_OVERRIDE_NAME, HEADER_KEY_CONTENT_ID_OVERRIDE_NAME_DEFAULT);
    }

    public String getKeyContentHashHeader() {
        return getStringOrDefault(HEADER_KEY_CONTENT_HASH_OVERRIDE_NAME, HEADER_KEY_CONTENT_HASH_OVERRIDE_NAME_DEFAULT);
    }

    public String getKeyGroupIdHeader() {
        return getStringOrDefault(HEADER_KEY_GROUP_ID_OVERRIDE_NAME, HEADER_KEY_GROUP_ID_OVERRIDE_NAME_DEFAULT);
    }

    public String getKeyArtifactIdHeader() {
        return getStringOrDefault(HEADER_KEY_ARTIFACT_ID_OVERRIDE_NAME, HEADER_KEY_ARTIFACT_ID_OVERRIDE_NAME_DEFAULT);
    }

    public String getKeyVersionHeader() {
        return getStringOrDefault(HEADER_KEY_VERSION_OVERRIDE_NAME, HEADER_KEY_VERSION_OVERRIDE_NAME_DEFAULT);
    }

    ////

    public String getValueGlobalIdHeader() {
        return getStringOrDefault(HEADER_VALUE_GLOBAL_ID_OVERRIDE_NAME, HEADER_VALUE_GLOBAL_ID_OVERRIDE_NAME_DEFAULT);
    }

    public String getValueContentIdHeader() {
        return getStringOrDefault(HEADER_VALUE_CONTENT_ID_OVERRIDE_NAME, HEADER_VALUE_CONTENT_ID_OVERRIDE_NAME_DEFAULT);
    }

    public String getValueContentHashHeader() {
        return getStringOrDefault(HEADER_VALUE_CONTENT_HASH_OVERRIDE_NAME, HEADER_VALUE_CONTENT_HASH_OVERRIDE_NAME_DEFAULT);
    }

    public String getValueGroupIdHeader() {
        return getStringOrDefault(HEADER_VALUE_GROUP_ID_OVERRIDE_NAME, HEADER_VALUE_GROUP_ID_OVERRIDE_NAME_DEFAULT);
    }

    public String getValueArtifactIdHeader() {
        return getStringOrDefault(HEADER_VALUE_ARTIFACT_ID_OVERRIDE_NAME, HEADER_VALUE_ARTIFACT_ID_OVERRIDE_NAME_DEFAULT);
    }

    public String getValueVersionHeader() {
        return getStringOrDefault(HEADER_VALUE_VERSION_OVERRIDE_NAME, HEADER_VALUE_VERSION_OVERRIDE_NAME_DEFAULT);
    }

    ////

    public String getKeyMessageTypeHeader() {
        return getStringOrDefault(HEADER_KEY_MESSAGE_TYPE_OVERRIDE_NAME, HEADER_KEY_MESSAGE_TYPE_OVERRIDE_NAME_DEFAULT);
    }

    public String getValueMessageTypeHeader() {
        return getStringOrDefault(HEADER_VALUE_MESSAGE_TYPE_OVERRIDE_NAME, HEADER_VALUE_MESSAGE_TYPE_OVERRIDE_NAME_DEFAULT);
    }
}
