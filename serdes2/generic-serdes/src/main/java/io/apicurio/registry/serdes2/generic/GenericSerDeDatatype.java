package io.apicurio.registry.serdes2.generic;

import io.apicurio.registry.resolver.ParsedSchema;
import io.apicurio.registry.resolver.SchemaParser;
import io.apicurio.registry.serdes2.generic.config.Configurable;
import io.apicurio.registry.serdes2.generic.record.GenericHeaders;

import java.io.OutputStream;
import java.nio.ByteBuffer;

public interface GenericSerDeDatatype<SCHEMA, DATA> extends Configurable {

    void writeData(GenericHeaders headers, ParsedSchema<SCHEMA> schema, DATA data, OutputStream out)
            throws Exception;

    DATA readData(GenericHeaders headers, ParsedSchema<SCHEMA> schema, ByteBuffer buffer);

    SchemaParser<SCHEMA, DATA> getSchemaParser();
}
