package io.apicurio.registry.serdes2.json.kafka;

import com.networknt.schema.JsonSchema;
import io.apicurio.registry.serdes2.json.JsonSerDeDatatype;
import io.apicurio.registry.serdes2.platform.kafka.ApicurioKafkaSerializer;

import java.util.Map;

public class JsonSchemaKafkaSerializer<DATA> extends ApicurioKafkaSerializer<DATA, JsonSchema> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        var dataType = new JsonSerDeDatatype<DATA>();
        super.configure(dataType, configs, isKey);
    }
}
