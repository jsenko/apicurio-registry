package io.apicurio.registry.serdes2.generic.coordhandler;

import io.apicurio.registry.serdes2.generic.coord.RegistryContentCoord;
import io.apicurio.registry.serdes2.generic.record.Record;

public interface RegistryContentCoordHandler extends RegistryContentReadCoordHandler {


    void writeCoord(Record<?> writeRecord, RegistryContentCoord coord);
}
