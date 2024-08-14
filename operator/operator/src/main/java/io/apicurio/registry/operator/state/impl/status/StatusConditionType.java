package io.apicurio.registry.operator.state.impl.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;

import static io.apicurio.registry.operator.state.impl.status.StatusConditionStatus.*;

@AllArgsConstructor
@Getter
@ToString
public enum StatusConditionType {

    // spotless:off
    READY("Ready", TRUE, false, null, null),
    OPERATOR_ERROR("OperatorError", FALSE, true, Duration.ofSeconds(10), Duration.ofSeconds(5)),
    CONFIGURATION_ERROR("ConfigurationError", FALSE, true, null, null);
    // CONFIGURATION_WARNING("ConfigurationWarning")
    // spotless:on

    private final String value;

    /**
     * Optional, may be null
     */
    private final StatusConditionStatus defaultStatus;

    /**
     * e.g. do not hide for Ready, hide for errors and warnings
     */
    private final boolean hideWhenDefaultStatus;

    /**
     * When the status should be cleared, unless it's recreated during reconciliation. May be null (TODO
     * Optional?)
     */
    private final Duration expiration;

    /**
     * Duration after which should the reconciliation be rescheduled. May be null (TODO Optional?)
     */
    private final Duration reschedule;
}
