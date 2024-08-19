package io.apicurio.registry.serdes2.platform.kafka;

import io.apicurio.registry.serdes2.generic.GenericDeserializer;
import io.apicurio.registry.serdes2.generic.SerDesException;
import io.apicurio.registry.serdes2.generic.config.SerDeConfigReader;
import io.apicurio.registry.serdes2.generic.GenericSerDeDatatype;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

import static io.apicurio.registry.serde.SerdeConfig.IS_KEY;

public abstract class ApicurioKafkaDeserializer<DATA, SCHEMA> implements Deserializer<DATA> {

    private GenericDeserializer<SCHEMA, DATA> genericDeserializer;

    public void configure(GenericSerDeDatatype<SCHEMA, DATA> datatype, Map<String, ?> configs,
            boolean isKey) {

        var config = new SerDeConfigReader((Map<String, Object>) configs);
        config.setConfig(IS_KEY, isKey);

        datatype.configure(config);

        genericDeserializer = new GenericDeserializer<>(datatype, null); // TODO
        genericDeserializer.configure(config);
    }

    @Override
    public DATA deserialize(String topic, byte[] data) {
        try {
            return genericDeserializer.deserialize(topic, null, data);
        } catch (SerDesException ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }
    }

    @Override
    public DATA deserialize(String topic, Headers headers, byte[] data) {
        try{
            return genericDeserializer.deserialize(topic, headers, data);
        } catch (SerDesException ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }
    }

    @Override
    public void close() {
        genericDeserializer.close();
    }
}
