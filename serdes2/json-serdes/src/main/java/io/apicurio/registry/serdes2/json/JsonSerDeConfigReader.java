package io.apicurio.registry.serdes2.json;

import io.apicurio.registry.serdes2.generic.config.GenericConfigReader;
import io.apicurio.registry.serdes2.generic.config.SerDeConfigReader;

import java.util.Map;

import static io.apicurio.registry.serde.SerdeConfig.*;
import static io.apicurio.registry.serde.SerdeConfig.DESERIALIZER_SPECIFIC_VALUE_RETURN_CLASS;

public class JsonSerDeConfigReader extends SerDeConfigReader {

    public JsonSerDeConfigReader(Map<String, Object> rawConfig) {
        super(rawConfig);
    }

    public JsonSerDeConfigReader(GenericConfigReader config) {
        super(config.getRawConfig());
    }

    public boolean isValidationEnabled() {
        return getBooleanOrDefault(VALIDATION_ENABLED, VALIDATION_ENABLED_DEFAULT);
    }

    public Class<?> getSpecificReturnClass() {
        if (isKey()) {
            return getClass(DESERIALIZER_SPECIFIC_KEY_RETURN_CLASS);
        } else {
            return getClass(DESERIALIZER_SPECIFIC_VALUE_RETURN_CLASS);
        }
    }
}
