package io.apicurio.registry.serdes2.generic.record.meta;

import io.apicurio.registry.serdes2.generic.record.GenericHeaders;

public interface HeaderLikeMeta extends RecordMetadata {

    GenericHeaders getHeaders();

    void setHeaders(GenericHeaders headers);
}
