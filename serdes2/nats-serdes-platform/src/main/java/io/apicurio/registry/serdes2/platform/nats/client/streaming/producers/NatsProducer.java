package io.apicurio.registry.serdes2.platform.nats.client.streaming.producers;

import io.apicurio.registry.serdes2.platform.nats.client.exceptions.ApicurioNatsException;

public interface NatsProducer<T> extends AutoCloseable {

    void send(T message) throws ApicurioNatsException;
}
