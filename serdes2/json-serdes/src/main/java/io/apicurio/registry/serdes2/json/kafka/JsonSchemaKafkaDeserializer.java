package io.apicurio.registry.serdes2.json.kafka;

import com.networknt.schema.JsonSchema;
import io.apicurio.registry.serdes2.json.JsonSerDeDatatype;
import io.apicurio.registry.serdes2.platform.kafka.ApicurioKafkaDeserializer;

import java.util.Map;

public class JsonSchemaKafkaDeserializer<DATA> extends ApicurioKafkaDeserializer<DATA, JsonSchema> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        var dataType = new JsonSerDeDatatype<DATA>();
        super.configure(dataType, configs, isKey);
    }
}
