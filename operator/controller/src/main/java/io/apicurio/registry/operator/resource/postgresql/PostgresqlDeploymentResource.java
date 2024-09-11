package io.apicurio.registry.operator.resource.postgresql;

import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

import static io.apicurio.registry.operator.resource.ResourceFactory.COMPONENT_POSTGRESQL;
import static io.apicurio.registry.operator.resource.ResourceKey.POSTGRESQL_DEPLOYMENT_KEY;

@KubernetesDependent(labelSelector = "app.kubernetes.io/name=apicurio-registry,app.kubernetes.io/component="
        + COMPONENT_POSTGRESQL, resourceDiscriminator = PostgresqlDeploymentDiscriminator.class)
public class PostgresqlDeploymentResource
        extends CRUDKubernetesDependentResource<Deployment, ApicurioRegistry3> {

    public PostgresqlDeploymentResource() {
        super(Deployment.class);
    }

    @Override
    protected Deployment desired(ApicurioRegistry3 primary, Context<ApicurioRegistry3> context) {
        return POSTGRESQL_DEPLOYMENT_KEY.getFactory().apply(primary);
    }
}
