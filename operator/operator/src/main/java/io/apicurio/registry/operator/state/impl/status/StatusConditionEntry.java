package io.apicurio.registry.operator.state.impl.status;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class StatusConditionEntry {

    private StatusConditionType type;

    @Default
    private StatusConditionStatus status = StatusConditionStatus.TRUE;

    // Reason is intended to be a one-word, CamelCase representation of the category of cause of the current
    // status
    // Use exception class name
    private String reason;

    // Message is intended to be presented to users in detailed status explanations, such as kubectl describe
    // output.
    private String message;
}
