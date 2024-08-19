package io.apicurio.registry.serdes2.generic;

import io.apicurio.registry.serdes2.generic.config.Configurable;

import java.io.Closeable;
import java.util.Map;
import java.util.function.Supplier;

public class Utils {

    private static ClassLoader getClassLoader() {
        var cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = Utils.class.getClassLoader();
        }
        return cl;
    }

    private static <T> Class<? extends T> loadClass(String klass, Class<T> superType) {
        try {
            return Class.forName(klass, true, getClassLoader()).asSubclass(superType);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T> T newInstance(Class<T> klass) {
        if (klass == null)
            throw new RuntimeException("class cannot be null");
        try {
            return klass.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Could not instantiate class " + klass.getCanonicalName(), ex);
        }
    }

    public static <T> T newConfiguredInstance(Object klass, Class<T> superType, Map<String, Object> rawConfig) {
        if (klass == null) {
            return null;
        }
        Object instance;
        if (klass instanceof String) {
            instance = newInstance(loadClass((String) klass, superType));
        } else if (klass instanceof Class<?>) {
            instance = newInstance((Class<?>) klass);
        } else {
            throw new RuntimeException("Unexpected element of type " + klass.getClass().getCanonicalName()
                    + ", expected String or Class");
        }
        try {
            if (!superType.isInstance(instance)) {
                throw new RuntimeException(klass.getClass().getCanonicalName() + " is not an instance of "
                        + superType.getCanonicalName());
            }
            if (rawConfig != null && instance instanceof Configurable) {
                ((org.apache.kafka.common.Configurable) instance).configure(rawConfig);
            }
        } catch (Exception ex) {
            if (instance instanceof Closeable) {
                try {
                    ((Closeable) instance).close();
                } catch (Throwable t) {
                    // TODO log
                }
            }
            throw ex;
        }
        return superType.cast(instance);
    }

    public static <T> T castOr(Object value, Class<T> targetType, Supplier<T> otherwise) {
        if (targetType.isInstance(value)) {
            return targetType.cast(value);
        } else {
            return otherwise.get();
        }
    }
}
