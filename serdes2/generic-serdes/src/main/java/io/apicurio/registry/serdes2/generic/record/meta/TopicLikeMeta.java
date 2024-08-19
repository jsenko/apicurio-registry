package io.apicurio.registry.serdes2.generic.record.meta;

public interface TopicLikeMeta extends RecordMetadata {

    String getTopic();

    void setTopic(String topic);
}
