package io.apicurio.registry.serdes2.platform.kafka.record;

import io.apicurio.registry.serdes2.generic.record.GenericHeaders;
import io.apicurio.registry.serdes2.generic.record.HeaderLikeMeta;
import io.apicurio.registry.serdes2.generic.record.TopicLikeMeta;

public class KafkaMessageRecord implements TopicLikeMeta, HeaderLikeMeta {

    private String kafkaTopic;

    private KafkaHeaders kafkaHeaders;

    @Override
    public GenericHeaders getHeaders() {
        return kafkaHeaders;
    }

    @Override
    public void setHeaders(GenericHeaders headers) {
        kafkaHeaders = new KafkaHeaders(headers);
    }

    @Override
    public String getTopic() {
        return kafkaTopic;
    }

    @Override
    public void setTopic(String topic) {
        kafkaTopic = topic;
    }
}
