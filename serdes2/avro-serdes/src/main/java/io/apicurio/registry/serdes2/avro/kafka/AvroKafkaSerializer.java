package io.apicurio.registry.serdes2.avro.kafka;

import io.apicurio.registry.serdes2.avro.AvroSerDeDatatype;
import io.apicurio.registry.serdes2.platform.kafka.ApicurioKafkaSerializer;
import org.apache.avro.Schema;

import java.util.Map;

public class AvroKafkaSerializer<DATA> extends ApicurioKafkaSerializer<DATA, Schema> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        var dataType = new AvroSerDeDatatype<DATA>();
        super.configure(dataType, configs, isKey);
    }
}
