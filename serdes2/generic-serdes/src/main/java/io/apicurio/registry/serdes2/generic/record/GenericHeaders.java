package io.apicurio.registry.serdes2.generic.record;

import java.util.List;

public interface GenericHeaders {

    GenericHeaders add(GenericHeader header);

    GenericHeaders add(String key, byte[] value);

    GenericHeader lastHeader(String key);

    List<GenericHeader> headers(String key);

    List<GenericHeader> headers();
}
