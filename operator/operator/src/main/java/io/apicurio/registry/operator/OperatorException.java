package io.apicurio.registry.operator;

import io.apicurio.registry.operator.state.impl.status.IntoStatusConditionEntry;
import io.apicurio.registry.operator.state.impl.status.StatusConditionEntry;
import io.apicurio.registry.operator.state.impl.status.StatusConditionStatus;
import io.apicurio.registry.operator.state.impl.status.StatusConditionType;

public class OperatorException extends RuntimeException implements IntoStatusConditionEntry {

    public OperatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperatorException(String message) {
        super(message);
    }

    public OperatorException(Throwable cause) {
        super(cause);
    }

    @Override
    public StatusConditionEntry intoStatusConditionEntry() {
        var reason = getClass().getSimpleName();
        var message = getMessage();

        if (getCause() != null) {
            reason = getCause().getClass().getSimpleName();
            message = getCause().getMessage();
        }
        // spotless:off
        return StatusConditionEntry.builder()
                .type(StatusConditionType.OPERATOR_ERROR)
                .status(StatusConditionStatus.TRUE)
                .reason(reason)
                .message(message)
                .build();
        // spotless:on
    }
}
