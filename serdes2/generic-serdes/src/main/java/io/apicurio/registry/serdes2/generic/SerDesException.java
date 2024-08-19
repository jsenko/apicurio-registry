package io.apicurio.registry.serdes2.generic;

public class SerDesException extends RuntimeException {

    public SerDesException(String message) {
        super(message);
    }

    public SerDesException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerDesException(Throwable cause) {
        super(cause);
    }
}
