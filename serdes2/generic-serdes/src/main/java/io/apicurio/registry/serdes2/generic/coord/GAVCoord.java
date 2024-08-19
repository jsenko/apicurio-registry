package io.apicurio.registry.serdes2.generic.coord;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class GAVCoord implements RegistryContentCoord { // TODO: Java record?

    @NonNull
    private final String groupId; // TODO: default groupId?

    @NonNull
    private final String artifactId;

    @NonNull
    private final String version;

    @Override
    public boolean isGAV() {
        return true;
    }
}
