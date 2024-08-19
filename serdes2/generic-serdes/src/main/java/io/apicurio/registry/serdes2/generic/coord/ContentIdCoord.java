package io.apicurio.registry.serdes2.generic.coord;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ContentIdCoord implements RegistryContentCoord { // TODO: Java record?

    private final long contentId;

    @Override
    public boolean isContentId() {
        return true;
    }
}
