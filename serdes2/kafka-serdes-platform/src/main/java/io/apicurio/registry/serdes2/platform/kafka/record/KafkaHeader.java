package io.apicurio.registry.serdes2.platform.kafka.record;

import io.apicurio.registry.serdes2.generic.record.GenericHeader;
import lombok.Getter;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

public class KafkaHeader implements GenericHeader {

    @Getter
    private final Header kafkaHeader;

    public KafkaHeader(Header kafkaHeader) {
        this.kafkaHeader = kafkaHeader;
    }

    public KafkaHeader(GenericHeader header) {
        this.kafkaHeader = new RecordHeader(header.key(), header.value());
    }

    @Override
    public String key() {
        return kafkaHeader.key();
    }

    @Override
    public byte[] value() {
        return kafkaHeader.value();
    }
}
