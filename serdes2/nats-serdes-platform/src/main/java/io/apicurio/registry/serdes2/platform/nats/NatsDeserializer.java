package io.apicurio.registry.serdes2.platform.nats;

public interface NatsDeserializer<DATA> extends Configurable {

    DATA deserialize(String subject, byte[] data);
}
