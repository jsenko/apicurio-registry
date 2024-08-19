package io.apicurio.registry.serdes2.generic.record;

import io.apicurio.registry.serdes2.generic.record.meta.RecordMetadata;

/**
 * Record defines an object that is known as the data or the payload of the record and it's associated
 * metadata. A record can be message to be sent or simply an object that can be serialized and deserialized.
 */
public interface ReadRecord {

    RecordMetadata metadata();

    byte[] payload();
}
