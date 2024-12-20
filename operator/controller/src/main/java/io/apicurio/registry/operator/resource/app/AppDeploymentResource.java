package io.apicurio.registry.operator.resource.app;

import io.apicurio.registry.operator.OperatorException;
import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.apicurio.registry.operator.feat.KafkaSql;
import io.apicurio.registry.operator.feat.PostgresSql;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.apicurio.registry.operator.resource.LabelDiscriminators.AppDeploymentDiscriminator;
import static io.apicurio.registry.operator.resource.ResourceFactory.APP_CONTAINER_NAME;
import static io.apicurio.registry.operator.resource.ResourceFactory.COMPONENT_APP;
import static io.apicurio.registry.operator.resource.ResourceKey.APP_DEPLOYMENT_KEY;
import static io.apicurio.registry.operator.resource.ResourceKey.STUDIO_UI_SERVICE_KEY;
import static io.apicurio.registry.operator.utils.Mapper.toYAML;
import static java.util.Objects.requireNonNull;

// spotless:off
@KubernetesDependent(
        labelSelector = "app.kubernetes.io/name=apicurio-registry,app.kubernetes.io/component=" + COMPONENT_APP,
        resourceDiscriminator = AppDeploymentDiscriminator.class
)
// spotless:on
public class AppDeploymentResource extends CRUDKubernetesDependentResource<Deployment, ApicurioRegistry3> {

    private static final Logger log = LoggerFactory.getLogger(AppDeploymentResource.class);

    public AppDeploymentResource() {
        super(Deployment.class);
    }

    @Override
    protected Deployment desired(ApicurioRegistry3 primary, Context<ApicurioRegistry3> context) {

        var d = APP_DEPLOYMENT_KEY.getFactory().apply(primary);

        var envVars = new LinkedHashMap<String, EnvVar>();
        primary.getSpec().getApp().getEnv().forEach(e -> {
            envVars.put(e.getName(), e);
        });

        // spotless:off
        addEnvVar(envVars, new EnvVarBuilder().withName("QUARKUS_PROFILE").withValue("prod").build());
        addEnvVar(envVars, new EnvVarBuilder().withName("QUARKUS_HTTP_ACCESS_LOG_ENABLED").withValue("true").build());
        addEnvVar(envVars, new EnvVarBuilder().withName("QUARKUS_HTTP_CORS_ORIGINS").withValue("*").build());
        // spotless:on

        // This is enabled only if Studio is deployed. It is based on Service in case a custom Ingress is
        // used.
        var sOpt = context.getSecondaryResource(STUDIO_UI_SERVICE_KEY.getKlass(),
                STUDIO_UI_SERVICE_KEY.getDiscriminator());
        sOpt.ifPresent(s -> {
            addEnvVar(envVars,
                    new EnvVarBuilder().withName("APICURIO_REST_MUTABILITY_ARTIFACT-VERSION-CONTENT_ENABLED")
                            .withValue("true").build());
        });

        if (!PostgresSql.configureDatasource(primary, envVars)) {
            KafkaSql.configureKafkaSQL(primary, envVars);
        }

        var container = getContainerFromDeployment(d, APP_CONTAINER_NAME);
        container.setEnv(envVars.values().stream().toList());

        log.debug("Desired {} is {}", APP_DEPLOYMENT_KEY.getId(), toYAML(d));
        return d;
    }

    public static void addEnvVar(Map<String, EnvVar> map, EnvVar envVar) {
        if (!map.containsKey(envVar.getName())) {
            map.put(envVar.getName(), envVar);
        }
    }

    /**
     * Get container with a given name from the given Deployment.
     *
     * @throws OperatorException if container was not found
     */
    public static Container getContainerFromDeployment(Deployment d, String name) {
        requireNonNull(d);
        requireNonNull(name);
        log.debug("Getting container {} in Deployment {}", name, ResourceID.fromResource(d));
        if (d.getSpec() != null & d.getSpec().getTemplate() != null) {
            var c = getContainerFromPodTemplateSpec(d.getSpec().getTemplate(), name);
            if (c != null) {
                return c;
            }
        }
        throw new OperatorException(
                "Container %s not found in Deployment %s".formatted(name, ResourceID.fromResource(d)));
    }

    /**
     * Get container with a given name from the given PTS.
     *
     * @return null when container was not found
     */
    public static Container getContainerFromPodTemplateSpec(PodTemplateSpec pts, String name) {
        requireNonNull(pts);
        requireNonNull(name);
        if (pts.getSpec() != null && pts.getSpec().getContainers() != null) {
            for (var c : pts.getSpec().getContainers()) {
                if (name.equals(c.getName())) {
                    return c;
                }
            }
        }
        return null;
    }
}
