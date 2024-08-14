package io.apicurio.registry.operator.action.impl.common;

import io.apicurio.registry.operator.action.AbstractAction;
import io.apicurio.registry.operator.action.ActionOrder;
import io.apicurio.registry.operator.context.CRContext;
import io.apicurio.registry.operator.state.impl.status.StatusConditionCache;
import jakarta.enterprise.context.ApplicationScoped;

import static io.apicurio.registry.operator.action.ActionOrder.ORDERING_LAST;
import static io.apicurio.registry.operator.resource.ResourceKey.REGISTRY_KEY;

@ApplicationScoped
public class ApplyStatusAction extends AbstractAction<StatusConditionCache> {

    @Override
    public ActionOrder ordering() {
        return ORDERING_LAST;
    }

    @Override
    public Class<StatusConditionCache> getStateClass() {
        return StatusConditionCache.class;
    }

    @Override
    public boolean shouldRun(StatusConditionCache state, CRContext crContext) {
        return state.isChanged();
    }

    @Override
    public void run(StatusConditionCache state, CRContext crContext) {
        crContext.withDesiredResource(REGISTRY_KEY, p -> {
            var conditions = state.getConditions(crContext, true);
            p.getStatus().setConditions(conditions.getItem1());
            if (conditions.getItem2() != null) {
                crContext.rescheduleSeconds(conditions.getItem2());
            }
        });
    }
}
