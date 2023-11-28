package io.apicurio.registry.storage.impl.sql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public enum SemverBranchingMode {

    DISABLED("disabled"),
    COERCE("coerce"),
    STRICT("strict");

    private final static Map<String, SemverBranchingMode> CONSTANTS = new HashMap<>();

    static {
        for (SemverBranchingMode c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;

    SemverBranchingMode(String value) {
        this.value = value;
    }

    @JsonCreator
    public static SemverBranchingMode fromValue(String value) {
        requireNonNull(value);
        SemverBranchingMode constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    @JsonValue
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value();
    }
}
