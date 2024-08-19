package io.apicurio.registry.serdes2.generic.headers;

import io.apicurio.registry.serdes2.generic.record.GenericHeader;
import io.apicurio.registry.serdes2.generic.record.GenericHeaders;
import io.apicurio.registry.utils.IoUtil;

import java.util.Map;

/**
 * Common utility class for serializers and deserializers that use config properties such as
 * {@link SerdeConfig#HEADER_VALUE_MESSAGE_TYPE_OVERRIDE_NAME}
 */
public class MessageTypeSerdeHeaders {

    private final String messageTypeHeaderName;

    public MessageTypeSerdeHeaders(Map<String, Object> rawConfig) {
        var config = new DefaultHeadersHandlerConfigReader(rawConfig);
        if (config.isKey()) {
            messageTypeHeaderName = config.getKeyMessageTypeHeader();
        } else {
            messageTypeHeaderName = config.getValueMessageTypeHeader();
        }
    }

    public String getMessageType(GenericHeaders headers) {
        GenericHeader header = headers.lastHeader(messageTypeHeaderName);
        if (header == null) {
            return null;
        }
        return IoUtil.toString(header.value());
    }

    public void addMessageTypeHeader(GenericHeaders headers, String messageType) {
        headers.add(messageTypeHeaderName, IoUtil.toBytes(messageType));
    }
}
