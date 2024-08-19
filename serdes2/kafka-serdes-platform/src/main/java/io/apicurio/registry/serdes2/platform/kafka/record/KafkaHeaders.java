package io.apicurio.registry.serdes2.platform.kafka.record;

import io.apicurio.registry.serdes2.generic.record.GenericHeader;
import io.apicurio.registry.serdes2.generic.record.GenericHeaders;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;

import java.util.ArrayList;
import java.util.List;

public class KafkaHeaders implements GenericHeaders {

    private final Headers kafkaHeaders;

    public KafkaHeaders(Headers kafkaHeaders) {
        this.kafkaHeaders = kafkaHeaders;
    }

    public KafkaHeaders(GenericHeaders headers) {
        kafkaHeaders = new RecordHeaders();
        headers.headers().forEach(this::add);
    }

    @Override
    public GenericHeaders add(GenericHeader header) {
        return add(header.key(), header.value());
    }

    @Override
    public GenericHeaders add(String key, byte[] value) {
        kafkaHeaders.add(key, value);
        return this;
    }

    @Override
    public GenericHeader lastHeader(String key) {
        return new KafkaHeader(kafkaHeaders.lastHeader(key));
    }

    @Override
    public List<GenericHeader> headers(String key) {
        var res = new ArrayList<GenericHeader>();
        for (Header kafkaHeader : kafkaHeaders.headers(key)) {
            res.add(new KafkaHeader(kafkaHeader));
        }
        return res;
    }

    @Override
    public List<GenericHeader> headers() {
        var res = new ArrayList<GenericHeader>();
        for (Header kafkaHeader : kafkaHeaders) {
            res.add(new KafkaHeader(kafkaHeader));
        }
        return res;
    }
}
