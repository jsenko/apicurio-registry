package io.apicurio.registry.serdes2.generic.coord;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ContentHashCoord implements RegistryContentCoord { // TODO: Java record?

    @NonNull
    private final String contentHash;

    @Override
    public boolean isContentHash() {
        return true;
    }
}
