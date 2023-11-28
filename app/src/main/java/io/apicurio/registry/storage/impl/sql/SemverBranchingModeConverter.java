package io.apicurio.registry.storage.impl.sql;

import org.eclipse.microprofile.config.spi.Converter;

public class SemverBranchingModeConverter implements Converter<SemverBranchingMode> {

    @Override
    public SemverBranchingMode convert(String value) throws IllegalArgumentException, NullPointerException {
        return SemverBranchingMode.fromValue(value);
    }
}
