package io.apicurio.registry.serdes2.generic;

import io.apicurio.registry.resolver.SchemaLookupResult;
import io.apicurio.registry.resolver.SchemaResolver;
import io.apicurio.registry.serde.data.KafkaSerdeMetadata;
import io.apicurio.registry.serde.data.KafkaSerdeRecord;
import io.apicurio.registry.serdes2.generic.record.Record;
import io.apicurio.registry.serdes2.generic.record.meta.HeaderLikeMeta;

import java.io.ByteArrayOutputStream;

public class GenericSerializer<SCHEMA, DATA> extends GenericSerDe<SCHEMA, DATA> {

    /**
     * @param serDeDatatype  required
     * @param schemaResolver may be null
     */
    public GenericSerializer(GenericSerDeDatatype<SCHEMA, DATA> serDeDatatype,
                             SchemaResolver<SCHEMA, DATA> schemaResolver) {
        super(serDeDatatype, schemaResolver);
    }

    public byte[] serialize(Record<DATA> record) {
        // just return null
        if (record == null || record.payload() == null) {
            return null;
        }
        try {

            KafkaSerdeMetadata resolverMetadata = new KafkaSerdeMetadata(topic, serDeConfig.isKey(), headers);

            SchemaLookupResult<SCHEMA> schema = getSchemaResolver()
                    .resolveSchema(new KafkaSerdeRecord<>(resolverMetadata, data));

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            var metadata = record.metadata();
            if (metadata instanceof HeaderLikeMeta) {
                var m = (HeaderLikeMeta) metadata;
                if (headersHandler != null && m.getHeaders() != null) {
                    headersHandler.writeHeaders(m.getHeaders(), schema.toArtifactReference());
                    serDeDatatype.writeData(m.getHeaders(), schema.getParsedSchema(), record.payload(), out);
                    return out.toByteArray();
                }
            } else {
                log.debug("Messaging platform does not support headers metadata.");
            }

            out.write(MAGIC_BYTE);
            getIdHandler().writeId(schema.toArtifactReference(), out);
            serDeDatatype.writeData(null, schema.getParsedSchema(), record.payload(), out);

            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
