package io.apicurio.registry.operator;

import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.apicurio.registry.operator.api.v1.status.Info;
import io.apicurio.registry.operator.resource.ResourceKey;
import io.apicurio.registry.operator.resource.app.AppDeploymentResource;
import io.apicurio.registry.operator.resource.app.AppIngressActivationCondition;
import io.apicurio.registry.operator.resource.app.AppIngressResource;
import io.apicurio.registry.operator.resource.app.AppServiceResource;
import io.apicurio.registry.operator.resource.postgresql.PostgresqlDeploymentResource;
import io.apicurio.registry.operator.resource.postgresql.PostgresqlServiceResource;
import io.apicurio.registry.operator.resource.ui.UIDeploymentResource;
import io.apicurio.registry.operator.resource.ui.UIIngressResource;
import io.apicurio.registry.operator.resource.ui.UIServiceResource;
import io.apicurio.registry.operator.state.impl.ClusterInfo;
import io.apicurio.registry.operator.util.HostUtil;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static io.apicurio.registry.operator.resource.ResourceFactory.COMPONENT_APP;
import static io.apicurio.registry.operator.resource.ResourceFactory.COMPONENT_UI;
import static io.apicurio.registry.operator.resource.ResourceKey.*;

// spotless:off
@ControllerConfiguration(
        dependents = {
                @Dependent(
                        type = PostgresqlDeploymentResource.class,
                        name = ResourceKey.POSTGRESQL_DEPLOYMENT_ID
                ),
                @Dependent(
                        type = PostgresqlServiceResource.class,
                        name = ResourceKey.POSTGRESQL_SERVICE_ID,
                        dependsOn = {ResourceKey.POSTGRESQL_DEPLOYMENT_ID}
                ),
                @Dependent(
                        type = AppDeploymentResource.class,
                        name = ResourceKey.APP_DEPLOYMENT_ID,
                        dependsOn = {ResourceKey.POSTGRESQL_SERVICE_ID}
                ),
                @Dependent(
                        type = AppServiceResource.class,
                        name = ResourceKey.APP_SERVICE_ID,
                        dependsOn = {ResourceKey.APP_DEPLOYMENT_ID}
                ),
                @Dependent(
                        type = AppIngressResource.class,
                        name = ResourceKey.APP_INGRESS_ID,
                        dependsOn = {ResourceKey.APP_SERVICE_ID},
                        activationCondition = AppIngressActivationCondition.class
                ),
                @Dependent(
                        type = UIDeploymentResource.class,
                        name = ResourceKey.UI_DEPLOYMENT_ID,
                        dependsOn = {ResourceKey.APP_SERVICE_ID}
                ),
                @Dependent(
                        type = UIServiceResource.class,
                        name = ResourceKey.UI_SERVICE_ID,
                        dependsOn = {ResourceKey.UI_DEPLOYMENT_ID}
                ),
                @Dependent(
                        type = UIIngressResource.class,
                        name = UI_INGRESS_ID,
                        dependsOn = {UI_SERVICE_ID}
                )
        }
)
// spotless:on
public class ApicurioRegistry3Reconciler implements Reconciler<ApicurioRegistry3>,
        ErrorStatusHandler<ApicurioRegistry3>, Cleaner<ApicurioRegistry3> {

    private static final Logger log = LoggerFactory.getLogger(ApicurioRegistry3Reconciler.class);

    @Inject
    ClusterInfo clusterInfo;

    @Inject
    HostUtil hostUtil;

    public UpdateControl<ApicurioRegistry3> reconcile(ApicurioRegistry3 primary,
            Context<ApicurioRegistry3> context) {

        log.info("Reconciling Apicurio Registry: {}", primary);

        var statusUpdater = new StatusUpdater(primary);

        return context
                .getSecondaryResource(APP_DEPLOYMENT_KEY.getKlass(), APP_DEPLOYMENT_KEY.getDiscriminator())
                .map(deployment -> {
                    log.info("Updating Apicurio Registry status");
                    primary.setStatus(statusUpdater.next(deployment));

                    if (clusterInfo.getCanonicalHost().isUnknown()) {
                        return UpdateControl.patchStatus(primary).rescheduleAfter(Duration.ofSeconds(10));
                    } else {
                        if (primary.getStatus().getInfo() == null) {
                            primary.getStatus().setInfo(new Info());
                        }
                        primary.getStatus().getInfo().setAppHost(hostUtil.getHost(COMPONENT_APP, primary));
                        primary.getStatus().getInfo().setUiHost(hostUtil.getHost(COMPONENT_UI, primary));
                        return UpdateControl.patchStatus(primary);
                    }

                }).orElseGet(UpdateControl::noUpdate);
    }

    @Override
    public ErrorStatusUpdateControl<ApicurioRegistry3> updateErrorStatus(ApicurioRegistry3 apicurioRegistry,
            Context<ApicurioRegistry3> context, Exception e) {
        var statusUpdater = new StatusUpdater(apicurioRegistry);
        apicurioRegistry.setStatus(statusUpdater.errorStatus(e));
        return ErrorStatusUpdateControl.updateStatus(apicurioRegistry);
    }

    @Override
    public DeleteControl cleanup(ApicurioRegistry3 primary, Context<ApicurioRegistry3> context) {
        return DeleteControl.defaultDelete();
    }
}
