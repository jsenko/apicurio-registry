package io.apicurio.registry.serdes2.generic.record;

import io.apicurio.registry.serdes2.generic.record.meta.RecordMetadata;

import java.nio.ByteBuffer;

/**
 * Record defines an object that is known as the data or the payload of the record and it's associated
 * metadata. A record can be message to be sent or simply an object that can be serialized and deserialized.
 */
public interface Record<DATA> {

    RecordMetadata metadata();

    DATA payload();

    // updatedata

    ByteBuffer rawPayload(); // outputstream vs bytebuff??

    // updaterawpayload
    // we can do buffer slicing, and marking as read only, updating views, etc.

    enum State {
        READING,
        READ,
        WRITING,
        WRITTEN;
    }
}
