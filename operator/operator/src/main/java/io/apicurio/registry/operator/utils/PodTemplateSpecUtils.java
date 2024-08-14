package io.apicurio.registry.operator.utils;

import io.apicurio.registry.operator.context.CRContext;
import io.apicurio.registry.operator.state.impl.status.StatusConditionCache;
import io.apicurio.registry.operator.state.impl.status.StatusConditionEntry;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

import static io.apicurio.registry.operator.state.impl.status.StatusConditionReasonConstants.CONFIGURATION_ERROR_REASON_UNSUPPORTED;
import static io.apicurio.registry.operator.state.impl.status.StatusConditionType.CONFIGURATION_ERROR;
import static io.apicurio.registry.operator.utils.TraverseUtils.*;

public class PodTemplateSpecUtils {

    private static final Logger log = LoggerFactory.getLogger(PodTemplateSpecUtils.class);

    public static void process(CRContext crContext, String containerName, PodTemplateSpec spec,
            PodTemplateSpec base) {
        // .metadata
        if (spec.getMetadata() == null) {
            spec.setMetadata(new ObjectMeta());
        }

        // .metadata.labels
        if (spec.getMetadata().getLabels() == null) {
            spec.getMetadata().setLabels(new HashMap<>());
        }
        mergeOverride(spec.getMetadata().getLabels(), base.getMetadata().getLabels());

        // .spec.containers[name = containerName]
        where(spec.getSpec().getContainers(), sc -> containerName.equals(sc.getName()), sc -> {
            where(base.getSpec().getContainers(), fc -> containerName.equals(fc.getName()), fc -> {

                if (!isEmpty(sc.getEnv())) {
                    // spotless:off
                    crContext.requireState(StatusConditionCache.class).updateStatusCondition(crContext,
                            StatusConditionEntry.builder()
                                    .type(CONFIGURATION_ERROR)
                                    .reason(CONFIGURATION_ERROR_REASON_UNSUPPORTED)
                                    .message(("Field '.spec.containers[name = %s].env' cannot be configured by PodTemplateSpec. "
                                              + "Use 'spec.*.env' instead.").formatted(containerName))
                                    .build()
                    );
                    // spotless:on
                }
                sc.setEnv(List.of()); // TODO: This might not even be necessary as it will be overridden

                if (isEmpty(sc.getImage())) {
                    sc.setImage(fc.getImage());
                }

                // TODO: These might be eventually moved from a factory into actions
                // .ports
                mergeNoOverride(sc.getPorts(), fc.getPorts(), ContainerPort::getContainerPort);

                // .readinessProbe
                if (sc.getReadinessProbe() == null) {
                    sc.setReadinessProbe(fc.getReadinessProbe());
                }

                // .livenessProbe
                if (sc.getLivenessProbe() == null) {
                    sc.setLivenessProbe(fc.getLivenessProbe());
                }

                // .resources
                if (sc.getResources() == null) {
                    sc.setResources(fc.getResources());
                } else {
                    // .resources.requests
                    if (sc.getResources().getRequests() == null) {
                        sc.getResources().setRequests(fc.getResources().getRequests());
                    } else {
                        mergeNoOverride(sc.getResources().getRequests(), fc.getResources().getRequests());
                    }
                    // .resources.limits
                    if (sc.getResources().getLimits() == null) {
                        sc.getResources().setLimits(fc.getResources().getLimits());
                    } else {
                        mergeNoOverride(sc.getResources().getLimits(), fc.getResources().getLimits());
                    }
                }
            });
        });
    }
}
