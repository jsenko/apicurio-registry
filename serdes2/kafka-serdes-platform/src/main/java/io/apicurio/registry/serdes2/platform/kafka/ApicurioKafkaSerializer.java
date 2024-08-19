package io.apicurio.registry.serdes2.platform.kafka;

import io.apicurio.registry.serdes2.generic.config.SerDeConfigReader;
import io.apicurio.registry.serdes2.generic.GenericSerDeDatatype;
import io.apicurio.registry.serdes2.generic.GenericSerializer;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

import static io.apicurio.registry.serde.SerdeConfig.IS_KEY;

public abstract class ApicurioKafkaSerializer<DATA, SCHEMA> implements Serializer<DATA> {

    private GenericSerializer<SCHEMA, DATA> genericSerializer;

    public void configure(GenericSerDeDatatype<SCHEMA, DATA> datatype, Map<String, ?> configs,
            boolean isKey) {

        var config = new SerDeConfigReader((Map<String, Object>) configs);
        config.setConfig(IS_KEY, isKey);

        datatype.configure(config);

        genericSerializer = new GenericSerializer<>(datatype, null); // TODO
        genericSerializer.configure(config);
    }

    @Override
    public byte[] serialize(String topic, DATA data) {
        return genericSerializer.serialize(topic, null, data);
    }

    @Override
    public byte[] serialize(String topic, Headers headers, DATA data) {
        // var gh = new GenericHeaders();
        // TODO: Headers
        return genericSerializer.serialize(topic, headers, data);
    }

    @Override
    public void close() {
        genericSerializer.close();
    }
}
