package io.apicurio.registry.serdes2.platform.nats;

public interface NatsSerializer<DATA> extends Configurable {

    byte[] serialize(String subject, DATA data);
}
