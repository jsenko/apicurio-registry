package io.apicurio.registry.serdes2.generic.config;

import io.apicurio.registry.serdes2.generic.Utils;

import java.util.HashMap;
import java.util.Map;

public class GenericConfigReader {

    // NOTE: The same config map can be shared between multiple config instances
    protected Map<String, Object> rawConfig;

    public GenericConfigReader(Map<String, Object> rawConfig) {
        this.rawConfig = new HashMap<>(rawConfig);
    }

    public Map<String, Object> getRawConfig() {
        return rawConfig;
    }

    public boolean hasConfig(String key) {
        return rawConfig.containsKey(key);
    }

    public void setConfig(String key, Object value) {
        rawConfig.put(key, value);
    }


    public Boolean asBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        throw new RuntimeException(String.format("Could not convert '%s' to Boolean.", value));
    }

    public Boolean getBooleanOrDefault(String key, Object _default) {
        var raw = rawConfig.getOrDefault(key, _default);
        return asBoolean(raw);
    }

    public Boolean getBoolean(String key) {
        return getBooleanOrDefault(key, null);
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        // TODO: Try toString() and issue a warning?
        throw new RuntimeException(String.format("Could not convert '%s' to String.", value));
    }

    public String getStringOrDefault(String key, Object _default) {
        var raw = rawConfig.getOrDefault(key, _default);
        return asString(raw);
    }

    public String getString(String key) {
        return getStringOrDefault(key, null);
    }

    public Class<?> asClass(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Class<?>) {
            return (Class<?>) value;
        }
        if (value instanceof String) {
            try {
                return io.apicurio.registry.resolver.utils.Utils.loadClass((String) value);
            } catch (Exception ex) {
                throw new RuntimeException("Could not convert '%s' to Class<?>.".formatted(value), ex);
            }
        }
        throw new RuntimeException("Could not convert '%s' to Class<?>.".formatted(value));
    }

    public Class<?> getClassOrDefault(String key, Object _default) {
        var raw = rawConfig.getOrDefault(key, _default);
        return asClass(raw);
    }

    public Class<?> getClass(String key) {
        return getClassOrDefault(key, null);
    }

    public <T> T getInstance(String key, Class<T> superType) {
        return Utils.newConfiguredInstance(rawConfig.get(key), superType, null);
    }

//    public <T> T getInstanceOrDefault(String key, Class<T> superType) {
//        return Utils.newConfiguredInstance(rawConfig.get(key), superType, null, this);
//    }
}
