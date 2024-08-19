package io.apicurio.registry.serdes2.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import io.apicurio.registry.resolver.ParsedSchema;
import io.apicurio.registry.resolver.SchemaParser;
import io.apicurio.registry.resolver.utils.Utils;
import io.apicurio.registry.serde.headers.MessageTypeSerdeHeaders;
import io.apicurio.registry.serde.jsonschema.JsonSchemaParser;
import io.apicurio.registry.serde.jsonschema.JsonSchemaValidationUtil;
import io.apicurio.registry.serdes2.generic.config.GenericConfigReader;
import io.apicurio.registry.serdes2.generic.GenericSerDeDatatype;
import org.apache.kafka.common.header.Headers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

import static io.apicurio.registry.serdes2.generic.Utils.castOr;

public class JsonSerDeDatatype<DATA> implements GenericSerDeDatatype<JsonSchema, DATA> {

    private ObjectMapper mapper;
    private final JsonSchemaParser<DATA> parser = new JsonSchemaParser<>();

    private JsonSerDeConfigReader config;

    private MessageTypeSerdeHeaders serdeHeaders;

    @Override
    public void configure(GenericConfigReader config) {
        this.config = castOr(config, JsonSerDeConfigReader.class, () -> new JsonSerDeConfigReader(config));

        serdeHeaders = new MessageTypeSerdeHeaders(this.config.getRawConfig(), this.config.isKey());

        if (null == mapper) {
            this.mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
    }

    @Override
    public void writeData(Headers headers, ParsedSchema<JsonSchema> schema, DATA data, OutputStream out)
            throws Exception {
        final byte[] dataBytes = mapper.writeValueAsBytes(data);
        if (config.isValidationEnabled()) {
            JsonSchemaValidationUtil.validateDataWithSchema(schema, dataBytes, mapper);
        }
        if (headers != null) {
            serdeHeaders.addMessageTypeHeader(headers, data.getClass().getName());
        }
        out.write(dataBytes);
    }

    @Override
    public DATA readData(Headers headers, ParsedSchema<JsonSchema> schema, ByteBuffer buffer) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        try {
            JsonParser parser = mapper.getFactory().createParser(data);

            if (config.isValidationEnabled()) {
                JsonSchemaValidationUtil.validateDataWithSchema(schema, data, mapper);
            }

            Class<DATA> messageType = null;

            if (config.getSpecificReturnClass() != null) {
                messageType = (Class<DATA>) config.getSpecificReturnClass();
            } else if (headers == null) {
                JsonNode jsonSchema = mapper.readTree(schema.getRawSchema());

                String javaType = null;
                JsonNode javaTypeNode = jsonSchema.get("javaType");
                if (javaTypeNode != null && !javaTypeNode.isNull()) {
                    javaType = javaTypeNode.textValue();
                }
                // TODO if javaType is null, maybe warn something like this?
                // You can try configure the property \"apicurio.registry.serde.json-schema.java-type\" with
                // the full class name to use for deserialization
                messageType = javaType == null ? null : Utils.loadClass(javaType);
            } else {
                String javaType = serdeHeaders.getMessageType(headers);
                messageType = javaType == null ? null : Utils.loadClass(javaType);
            }

            if (messageType == null) {
                // TODO maybe warn there is no message type and the deserializer will return a JsonNode
                return (DATA) mapper.readTree(parser);
            } else {
                return mapper.readValue(parser, messageType);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public SchemaParser<JsonSchema, DATA> getSchemaParser() {
        return parser;
    }
}
