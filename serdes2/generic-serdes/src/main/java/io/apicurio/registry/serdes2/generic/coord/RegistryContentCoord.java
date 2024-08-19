package io.apicurio.registry.serdes2.generic.coord;


public interface RegistryContentCoord {

    default boolean isGAV() {
        return false;
    }

    default boolean isGlobalId() {
        return false;
    }

    default boolean isContentId() {
        return false;
    }

    default boolean isContentHash() {
        return false;
    }
}
