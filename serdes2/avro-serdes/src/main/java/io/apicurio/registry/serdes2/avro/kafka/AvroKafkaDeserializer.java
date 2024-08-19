package io.apicurio.registry.serdes2.avro.kafka;

import io.apicurio.registry.serdes2.avro.AvroSerDeDatatype;
import io.apicurio.registry.serdes2.platform.kafka.ApicurioKafkaDeserializer;
import org.apache.avro.Schema;

import java.util.Map;

public class AvroKafkaDeserializer<DATA> extends ApicurioKafkaDeserializer<DATA, Schema> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        var dataType = new AvroSerDeDatatype<DATA>();
        super.configure(dataType, configs, isKey);
    }
}
