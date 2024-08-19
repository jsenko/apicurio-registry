package io.apicurio.registry.serdes2.avro;

import io.apicurio.registry.serde.avro.AvroDatumProvider;
import io.apicurio.registry.serde.avro.AvroEncoding;
import io.apicurio.registry.serdes2.generic.config.GenericConfigReader;
import io.apicurio.registry.serdes2.generic.config.SerDeConfigReader;

import java.util.Map;

import static io.apicurio.registry.serde.avro.AvroKafkaSerdeConfig.AVRO_DATUM_PROVIDER;
import static io.apicurio.registry.serde.avro.AvroKafkaSerdeConfig.AVRO_ENCODING;

public class AvroSerDeConfigReader extends SerDeConfigReader {

    public AvroSerDeConfigReader(Map<String, Object> rawConfig) {
        super(rawConfig);
    }

    public AvroSerDeConfigReader(GenericConfigReader config) {
        super(config.getRawConfig());
    }

    public AvroEncoding getAvroEncoding() {
        return AvroEncoding.valueOf(getString(AVRO_ENCODING));
    }

    public AvroDatumProvider getAvroDatumProvider() {
        return getInstance(AVRO_DATUM_PROVIDER, AvroDatumProvider.class);
    }
}
