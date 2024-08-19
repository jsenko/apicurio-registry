package io.apicurio.registry.serdes2.generic.coord;

import io.apicurio.registry.exception.UnreachableCodeException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@Getter
@EqualsAndHashCode
@ToString
public class RegistryContentCoords {

    private GlobalIdCoord globalIdCoord;

    private ContentIdCoord contentIdCoord;

    private ContentHashCoord contentHashCoord;

    private GAVCoord gavCoord;

    public RegistryContentCoords(RegistryContentCoord... coords) {
        Arrays.stream(coords).forEach(c -> {
            // spotless:off
            if (c instanceof GlobalIdCoord) with((GlobalIdCoord) c);
            else if (c instanceof ContentIdCoord) with((ContentIdCoord) c);
            else if (c instanceof ContentHashCoord) with((ContentHashCoord) c);
            else if (c instanceof GAVCoord) with((GAVCoord) c);
            else throw new UnreachableCodeException();
            // spotless:on
        });
    }

    public RegistryContentCoords with(GlobalIdCoord coord) {
        globalIdCoord = coord;
        return this;
    }

    public RegistryContentCoords with(ContentIdCoord coord) {
        contentIdCoord = coord;
        return this;
    }

    public RegistryContentCoords with(ContentHashCoord coord) {
        contentHashCoord = coord;
        return this;
    }

    public RegistryContentCoords with(GAVCoord coord) {
        gavCoord = coord;
        return this;
    }
}
