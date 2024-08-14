package io.apicurio.registry.operator.action.impl.common;

import io.apicurio.registry.operator.action.AbstractAction;
import io.apicurio.registry.operator.action.ActionOrder;
import io.apicurio.registry.operator.context.CRContext;
import io.apicurio.registry.operator.state.impl.status.StatusConditionCache;
import io.apicurio.registry.operator.state.impl.status.StatusConditionEntry;
import io.apicurio.registry.operator.state.impl.status.StatusConditionType;
import jakarta.enterprise.context.ApplicationScoped;

import static io.apicurio.registry.operator.action.ActionOrder.ORDERING_EARLY;

@ApplicationScoped
public class InitStatusAction extends AbstractAction<StatusConditionCache> {

    @Override
    public ActionOrder ordering() {
        return ORDERING_EARLY;
    }

    @Override
    public Class<StatusConditionCache> getStateClass() {
        return StatusConditionCache.class;
    }

    @Override
    public StatusConditionCache initialize(CRContext crContext) {
        return new StatusConditionCache();
    }

    @Override
    public void run(StatusConditionCache state, CRContext crContext) {
        // spotless:off
        state.updateStatusCondition(crContext, StatusConditionEntry.builder()
                .type(StatusConditionType.READY)
                .reason("TODO")
                .message("Operand is initialized.")
                .build());
        // spotless:on
    }
}
