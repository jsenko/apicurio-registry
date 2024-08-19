package io.apicurio.registry.serdes2.generic.config;

import io.apicurio.registry.serdes2.generic.SerDesException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@AllArgsConstructor
@Getter
public enum IdOption {

    GLOBAL_ID("globalId"),
    CONTENT_ID("contentId");

    private final String value;

    private static final Map<String, IdOption> values;

    static {
        values = Arrays.stream(values()).collect(toMap(IdOption::getValue, v -> v));
    }

    public static IdOption of(String value) {
        var res = values.get(value);
        if (res == null) {
            throw new SerDesException("Unknown IdOption value: " + value);
        }
        return res;
    }
}
