package io.apicurio.registry.operator.resource.app;

import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

import java.util.ArrayList;
import java.util.List;

import static io.apicurio.registry.operator.resource.ResourceFactory.COMPONENT_APP;
import static io.apicurio.registry.operator.resource.ResourceKey.APP_DEPLOYMENT_KEY;
import static io.apicurio.registry.operator.resource.ResourceKey.POSTGRESQL_SERVICE_KEY;

@KubernetesDependent(labelSelector = "app.kubernetes.io/name=apicurio-registry,app.kubernetes.io/component="
        + COMPONENT_APP, resourceDiscriminator = AppDeploymentDiscriminator.class)
public class AppDeploymentResource extends CRUDKubernetesDependentResource<Deployment, ApicurioRegistry3> {

    public AppDeploymentResource() {
        super(Deployment.class);
    }

    @Override
    protected Deployment desired(ApicurioRegistry3 primary, Context<ApicurioRegistry3> context) {

        var d = APP_DEPLOYMENT_KEY.getFactory().apply(primary);

        var appEnv = new ArrayList<>(List.of(
                // spotless:off
                new EnvVarBuilder().withName("QUARKUS_PROFILE").withValue("prod").build(),
                new EnvVarBuilder().withName("APICURIO_CONFIG_CACHE_ENABLED").withValue("true").build(),
                new EnvVarBuilder().withName("QUARKUS_HTTP_ACCESS_LOG_ENABLED").withValue("true").build(),
                new EnvVarBuilder().withName("QUARKUS_HTTP_CORS_ORIGINS").withValue("*").build(),
                new EnvVarBuilder().withName("APICURIO_REST_DELETION_GROUP_ENABLED").withValue("true").build(),
                new EnvVarBuilder().withName("APICURIO_REST_DELETION_ARTIFACT_ENABLED").withValue("true").build(),
                new EnvVarBuilder().withName("APICURIO_REST_DELETION_ARTIFACTVERSION_ENABLED").withValue("true").build(),
                new EnvVarBuilder().withName("APICURIO_APIS_V2_DATE_FORMAT").withValue("yyyy-MM-dd''T''HH:mm:ssZ").build()
                // spotless:on
        ));

        var sOpt = context.getSecondaryResource(POSTGRESQL_SERVICE_KEY.getKlass(),
                POSTGRESQL_SERVICE_KEY.getDiscriminator());
        sOpt.ifPresent(s -> {
            appEnv.addAll(List.of(
                    // spotless:off
                    new EnvVarBuilder().withName("APICURIO_STORAGE_KIND").withValue("sql").build(),
                    new EnvVarBuilder().withName("APICURIO_STORAGE_SQL_KIND").withValue("postgresql").build(),
                    new EnvVarBuilder().withName("APICURIO_DATASOURCE_USERNAME").withValue("apicurio-registry").build(),
                    new EnvVarBuilder().withName("APICURIO_DATASOURCE_PASSWORD").withValue("password").build(),
                    new EnvVarBuilder().withName("APICURIO_DATASOURCE_URL").withValue(
                            "jdbc:postgresql://%s.%s.svc.cluster.local:5432/apicurio-registry"
                                    .formatted(s.getMetadata().getName(), s.getMetadata().getNamespace())
                    ).build()));
                    // spotless:on
        });

        d.getSpec().getTemplate().getSpec().getContainers().get(0).setEnv(appEnv);
        return d;
    }
}
