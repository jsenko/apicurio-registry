package io.apicurio.registry.serdes2.json;

import io.apicurio.registry.serde.avro.AvroDatumProvider;
import io.apicurio.registry.serde.avro.AvroEncoding;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericAvroSerializerConfig {

    AvroEncoding encoding;
    AvroDatumProvider<?> avroDatumProvider;
}
