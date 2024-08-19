package io.apicurio.registry.serdes2.generic.coord;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class GlobalIdCoord implements RegistryContentCoord { // TODO: Java record?

    private final long globalId;

    @Override
    public boolean isGlobalId() {
        return true;
    }
}
