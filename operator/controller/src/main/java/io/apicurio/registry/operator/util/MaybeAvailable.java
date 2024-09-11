package io.apicurio.registry.operator.util;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.function.Consumer;

import static io.apicurio.registry.operator.util.MaybeAvailable.State.*;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class MaybeAvailable<T> {

    private final State state;

    private final T value;

    private MaybeAvailable(State state, T value) {
        this.state = state;
        this.value = value;
    }

    public boolean isUnknown() {
        return state == UNKNOWN;
    }

    public boolean isNotAvailable() {
        return state == NOT_AVAILABLE;
    }

    public boolean isAvailable() {
        return state == AVAILABLE;
    }

    public T getValue() {
        requireNonNull(value);
        return value;
    }

    public void ifAvailable(Consumer<? super T> action) {
        if (isAvailable()) {
            action.accept(value);
        }
    }

    public static <T> MaybeAvailable<T> unknown() {
        return new MaybeAvailable<>(UNKNOWN, null);
    }

    public static <T> MaybeAvailable<T> notAvailable() {
        return new MaybeAvailable<>(NOT_AVAILABLE, null);
    }

    public static <T> MaybeAvailable<T> available(T value) {
        requireNonNull(value);
        return new MaybeAvailable<>(AVAILABLE, value);
    }

    public static <T> MaybeAvailable<T> ofNullable(T value) {
        if (value != null) {
            return available(value);
        } else {
            return notAvailable();
        }
    }

    public enum State {
        // spotless:off
        UNKNOWN,
        NOT_AVAILABLE,
        AVAILABLE
        // spotless:on
    }
}
