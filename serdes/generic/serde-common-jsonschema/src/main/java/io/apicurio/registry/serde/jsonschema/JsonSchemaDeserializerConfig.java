package io.apicurio.registry.serde.jsonschema;

import io.apicurio.registry.serde.config.SerdeConfig;

import java.util.HashMap;
import java.util.Map;

public class JsonSchemaDeserializerConfig extends SerdeConfig {

    private boolean isKey;

    /**
     * Constructor.
     *
     * @param originals
     */
    public JsonSchemaDeserializerConfig(Map<String, ?> originals) {
        Map<String, Object> joint = new HashMap<>(getDefaults());
        joint.putAll(originals);
        this.originals = joint;
    }

    public JsonSchemaDeserializerConfig(Map<String, ?> originals, boolean isKey) {
        Map<String, Object> joint = new HashMap<>(getDefaults());
        joint.putAll(originals);
        this.originals = joint;
        this.isKey = isKey;
    }

    public Class<?> getSpecificReturnClass() {
        if (isKey) {
            return this.getClass(DESERIALIZER_SPECIFIC_KEY_RETURN_CLASS);
        } else {
            return this.getClass(DESERIALIZER_SPECIFIC_VALUE_RETURN_CLASS);
        }
    }

    public boolean validationEnabled() {
        return this.getBoolean(VALIDATION_ENABLED);
    }

    @Override
    protected Map<String, ?> getDefaults() {
        Map<String, Object> joint = new HashMap<>(super.getDefaults());
        joint.putAll(DEFAULTS);
        return joint;
    }

    private static final Map<String, ?> DEFAULTS = Map.of(VALIDATION_ENABLED, VALIDATION_ENABLED_DEFAULT);
}
