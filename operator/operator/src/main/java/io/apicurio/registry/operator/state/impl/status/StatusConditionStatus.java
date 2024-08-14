package io.apicurio.registry.operator.state.impl.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor(access = PRIVATE)
@Getter
@ToString
public enum StatusConditionStatus {

    // spotless:off
    TRUE("True"),
    FALSE("False");
    // spotless:on

    private final String value;
}
