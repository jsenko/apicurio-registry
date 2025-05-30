package io.apicurio.registry.operator.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.apicurio.registry.operator.Configuration;
import io.apicurio.registry.operator.OperatorException;
import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.apicurio.registry.operator.api.v1.ApicurioRegistry3Spec;
import io.apicurio.registry.operator.api.v1.spec.AppSpec;
import io.apicurio.registry.operator.api.v1.spec.TLSSpec;
import io.apicurio.registry.operator.api.v1.spec.UiSpec;
import io.apicurio.registry.operator.status.StatusManager;
import io.apicurio.registry.operator.status.ValidationErrorConditionManager;
import io.apicurio.registry.operator.utils.SecretKeyRefTool;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicy;
import io.fabric8.kubernetes.api.model.policy.v1.PodDisruptionBudget;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.apicurio.registry.operator.Constants.*;
import static io.apicurio.registry.operator.api.v1.ContainerNames.REGISTRY_APP_CONTAINER_NAME;
import static io.apicurio.registry.operator.api.v1.ContainerNames.REGISTRY_UI_CONTAINER_NAME;
import static io.apicurio.registry.operator.resource.Labels.getSelectorLabels;
import static io.apicurio.registry.operator.resource.app.AppDeploymentResource.getContainerFromPodTemplateSpec;
import static io.apicurio.registry.operator.utils.Mapper.YAML_MAPPER;
import static io.apicurio.registry.operator.utils.Utils.isBlank;
import static java.util.Optional.ofNullable;

public class ResourceFactory {

    public static final ResourceFactory INSTANCE = new ResourceFactory();

    public static final String COMPONENT_APP = "app";
    public static final String COMPONENT_UI = "ui";

    // TODO: Merge the two sets of constants into an enum. Also consider including container names.
    public static final String COMPONENT_APP_SPEC_FIELD_NAME = "app";
    public static final String COMPONENT_UI_SPEC_FIELD_NAME = "ui";

    public static final String RESOURCE_TYPE_DEPLOYMENT = "deployment";
    public static final String RESOURCE_TYPE_SERVICE = "service";
    public static final String RESOURCE_TYPE_INGRESS = "ingress";
    public static final String RESOURCE_TYPE_POD_DISRUPTION_BUDGET = "poddisruptionbudget";
    public static final String RESOURCE_TYPE_NETWORK_POLICY = "networkpolicy";

    public Deployment getDefaultAppDeployment(ApicurioRegistry3 primary) {
        var r = initDefaultDeployment(primary, COMPONENT_APP,
                Optional.ofNullable(primary.getSpec()).map(ApicurioRegistry3Spec::getApp)
                        .map(AppSpec::getReplicas).orElse(DEFAULT_REPLICAS),
                ofNullable(primary.getSpec()).map(ApicurioRegistry3Spec::getApp)
                        .map(AppSpec::getPodTemplateSpec).orElse(null)); // TODO:

        var readinessProbe = DEFAULT_READINESS_PROBE;
        var livenessProbe = DEFAULT_LIVENESS_PROBE;
        var containerPort = List.of(new ContainerPortBuilder().withName("http").withProtocol("TCP").withContainerPort(8080).build());

        Optional<TLSSpec> tlsSpec = ofNullable(primary.getSpec())
                .map(ApicurioRegistry3Spec::getApp)
                .map(AppSpec::getTls);

        if (tlsSpec.isPresent()) {
            var keystore = new SecretKeyRefTool(tlsSpec.map(TLSSpec::getKeystoreSecretRef)
                    .orElse(null), "keystore");

            var keystorePassword = new SecretKeyRefTool(tlsSpec.map(TLSSpec::getKeystorePasswordSecretRef)
                    .orElse(null), "password");

            if (keystore.isValid() && keystorePassword.isValid()) {
                readinessProbe = TLS_DEFAULT_READINESS_PROBE;
                livenessProbe = TLS_DEFAULT_LIVENESS_PROBE;
                containerPort = List.of(new ContainerPortBuilder().withName("https").withProtocol("TCP").withContainerPort(8443).build());
            }
        }

        // Replicas
        mergeDeploymentPodTemplateSpec(
                COMPONENT_APP_SPEC_FIELD_NAME,
                primary,
                r.getSpec().getTemplate(),
                REGISTRY_APP_CONTAINER_NAME,
                Configuration.getAppImage(),
                containerPort,
                readinessProbe,
                livenessProbe,
                Map.of("cpu", new Quantity("500m"), "memory", new Quantity("512Mi")),
                Map.of("cpu", new Quantity("1"), "memory", new Quantity("1Gi"))
        );

        addDefaultLabels(r.getMetadata().getLabels(), primary, COMPONENT_APP);
        addSelectorLabels(r.getSpec().getSelector().getMatchLabels(), primary, COMPONENT_APP);
        addDefaultLabels(r.getSpec().getTemplate().getMetadata().getLabels(), primary, COMPONENT_APP);
        return r;
    }

    public Deployment getDefaultUIDeployment(ApicurioRegistry3 primary) {
        var r = initDefaultDeployment(primary, COMPONENT_UI,
                Optional.ofNullable(primary.getSpec()).map(ApicurioRegistry3Spec::getUi)
                        .map(UiSpec::getReplicas).orElse(DEFAULT_REPLICAS),
                ofNullable(primary.getSpec()).map(ApicurioRegistry3Spec::getUi)
                        .map(UiSpec::getPodTemplateSpec).orElse(null)); // TODO:
        // Replicas
        mergeDeploymentPodTemplateSpec(
                COMPONENT_UI_SPEC_FIELD_NAME,
                primary,
                r.getSpec().getTemplate(),
                REGISTRY_UI_CONTAINER_NAME,
                Configuration.getUIImage(),
                List.of(new ContainerPortBuilder().withName("http").withProtocol("TCP").withContainerPort(8080).build()),
                new ProbeBuilder().withHttpGet(new HTTPGetActionBuilder().withPath("/config.js").withPort(new IntOrString(8080)).withScheme("HTTP").build()).build(),
                new ProbeBuilder().withHttpGet(new HTTPGetActionBuilder().withPath("/config.js").withPort(new IntOrString(8080)).withScheme("HTTP").build()).build(),
                Map.of("cpu", new Quantity("100m"), "memory", new Quantity("256Mi")),
                Map.of("cpu", new Quantity("200m"), "memory", new Quantity("512Mi"))
        );
        addDefaultLabels(r.getMetadata().getLabels(), primary, COMPONENT_UI);
        addSelectorLabels(r.getSpec().getSelector().getMatchLabels(), primary, COMPONENT_UI);
        addDefaultLabels(r.getSpec().getTemplate().getMetadata().getLabels(), primary, COMPONENT_UI);
        return r;
    }

    private static Deployment initDefaultDeployment(ApicurioRegistry3 primary, String componentId,
                                                    int replicas, PodTemplateSpec pts) {
        var r = new Deployment();
        r.setMetadata(new ObjectMeta());
        r.getMetadata().setNamespace(primary.getMetadata().getNamespace());
        r.getMetadata().setName(
                primary.getMetadata().getName() + "-" + componentId + "-" + RESOURCE_TYPE_DEPLOYMENT);
        r.setSpec(new DeploymentSpec());
        r.getSpec().setReplicas(replicas);
        r.getSpec().setSelector(new LabelSelector());
        if (pts != null) {
            r.getSpec().setTemplate(pts);
        }
        else {
            r.getSpec().setTemplate(new PodTemplateSpec());
        }
        return r;
    }

    /**
     * Merge default values for a Deployment into the target PTS (from spec).
     */
    private static void mergeDeploymentPodTemplateSpec(
            String componentFieldName,
            ApicurioRegistry3 primary,
            PodTemplateSpec target,
            String containerName,
            String image,
            List<ContainerPort> ports,
            Probe readinessProbe,
            Probe livenessProbe,
            Map<String, Quantity> requests,
            Map<String, Quantity> limits
    ) {
        if (target.getMetadata() == null) {
            target.setMetadata(new ObjectMeta());
        }
        var c = getContainerFromPodTemplateSpec(target, containerName);
        if (c == null) {
            if (target.getSpec() == null) {
                target.setSpec(new PodSpec());
            }
            c = new Container();
            c.setName(containerName);
            if (target.getSpec().getContainers() == null) {
                target.getSpec().setContainers(new ArrayList<>());
            }
            target.getSpec().getContainers().add(c);
        }
        if (isBlank(c.getImage())) {
            c.setImage(image);
        }
        if (c.getEnv() != null && !c.getEnv().isEmpty()) {
            StatusManager.get(primary).getConditionManager(ValidationErrorConditionManager.class)
                    .recordError("""
                            Field spec.%s.podTemplateSpec.spec.containers[name = %s].env must be empty. \
                            Use spec.%s.env to configure environment variables.""", componentFieldName, containerName, componentFieldName);
        }
        if (c.getPorts() == null) {
            c.setPorts(new ArrayList<>());
        }
        var targetPorts = c.getPorts();
        ports.forEach(sourcePort -> {
            if (targetPorts.stream()
                    .noneMatch(targetPort -> sourcePort.getName().equals(targetPort.getName()))) {
                targetPorts.add(sourcePort);
            }
        });
        if (c.getReadinessProbe() == null) {
            c.setReadinessProbe(readinessProbe);
        }
        if (c.getLivenessProbe() == null) {
            c.setLivenessProbe(livenessProbe);
        }
        if (c.getResources() == null) {
            c.setResources(new ResourceRequirements());
            c.getResources().setRequests(requests);
            c.getResources().setLimits(limits);
        }
    }

    public Service getDefaultAppService(ApicurioRegistry3 primary) {
        var r = getDefaultResource(primary, Service.class, RESOURCE_TYPE_SERVICE, COMPONENT_APP);
        addSelectorLabels(r.getSpec().getSelector(), primary, COMPONENT_APP);
        return r;
    }

    public Service getDefaultUIService(ApicurioRegistry3 primary) {
        var r = getDefaultResource(primary, Service.class, RESOURCE_TYPE_SERVICE, COMPONENT_UI);
        addSelectorLabels(r.getSpec().getSelector(), primary, COMPONENT_UI);
        return r;
    }

    public Ingress getDefaultAppIngress(ApicurioRegistry3 primary) {
        var r = getDefaultResource(primary, Ingress.class, RESOURCE_TYPE_INGRESS, COMPONENT_APP);
        r.getSpec().getRules().get(0).getHttp().getPaths().get(0).getBackend().getService()
                .setName(primary.getMetadata().getName() + "-" + COMPONENT_APP + "-" + RESOURCE_TYPE_SERVICE);
        return r;
    }

    public Ingress getDefaultUIIngress(ApicurioRegistry3 primary) {
        var r = getDefaultResource(primary, Ingress.class, RESOURCE_TYPE_INGRESS, COMPONENT_UI);
        r.getSpec().getRules().get(0).getHttp().getPaths().get(0).getBackend().getService()
                .setName(primary.getMetadata().getName() + "-" + COMPONENT_UI + "-" + RESOURCE_TYPE_SERVICE);
        return r;
    }

    public PodDisruptionBudget getDefaultAppPodDisruptionBudget(ApicurioRegistry3 primary) {
        var pdb = getDefaultResource(primary, PodDisruptionBudget.class, RESOURCE_TYPE_POD_DISRUPTION_BUDGET,
                COMPONENT_APP);
        addSelectorLabels(pdb.getSpec().getSelector().getMatchLabels(), primary, COMPONENT_APP);
        return pdb;
    }

    public PodDisruptionBudget getDefaultUIPodDisruptionBudget(ApicurioRegistry3 primary) {
        var pdb = getDefaultResource(primary, PodDisruptionBudget.class, RESOURCE_TYPE_POD_DISRUPTION_BUDGET,
                COMPONENT_UI);
        addSelectorLabels(pdb.getSpec().getSelector().getMatchLabels(), primary, COMPONENT_UI);
        return pdb;
    }

    private <T extends HasMetadata> T getDefaultResource(ApicurioRegistry3 primary, Class<T> klass,
                                                         String resourceType, String component) {
        var r = deserialize("/k8s/default/" + component + "." + resourceType + ".yaml", klass);
        r.getMetadata().setNamespace(primary.getMetadata().getNamespace());
        r.getMetadata().setName(primary.getMetadata().getName() + "-" + component + "-" + resourceType);
        addDefaultLabels(r.getMetadata().getLabels(), primary, component);
        return r;
    }

    public NetworkPolicy getDefaultAppNetworkPolicy(ApicurioRegistry3 primary) {
        var networkPolicy = getDefaultResource(primary, NetworkPolicy.class, RESOURCE_TYPE_NETWORK_POLICY,
                COMPONENT_APP);
        addSelectorLabels(networkPolicy.getSpec().getPodSelector().getMatchLabels(), primary, COMPONENT_APP);
        return networkPolicy;
    }

    public NetworkPolicy getDefaultUINetworkPolicy(ApicurioRegistry3 primary) {
        var networkPolicy = getDefaultResource(primary, NetworkPolicy.class, RESOURCE_TYPE_NETWORK_POLICY,
                COMPONENT_UI);
        addSelectorLabels(networkPolicy.getSpec().getPodSelector().getMatchLabels(), primary, COMPONENT_UI);
        return networkPolicy;
    }

    private void addDefaultLabels(Map<String, String> labels, ApicurioRegistry3 primary, String component) {
        labels.putAll(Map.of(
                "app", primary.getMetadata().getName(),
                "app.kubernetes.io/name", "apicurio-registry",
                "app.kubernetes.io/component", component,
                "app.kubernetes.io/instance", primary.getMetadata().getName(),
                "app.kubernetes.io/version", Configuration.getRegistryVersion(),
                "app.kubernetes.io/part-of", "apicurio-registry",
                "app.kubernetes.io/managed-by", "apicurio-registry-operator"
        ));
    }

    private void addSelectorLabels(Map<String, String> labels, ApicurioRegistry3 primary, String component) {
        labels.putAll(getSelectorLabels(primary, component));
    }

    public static <T> T deserialize(String path, Class<T> klass) {
        try {
            return YAML_MAPPER.readValue(load(path), klass);
        }
        catch (JsonProcessingException ex) {
            throw new OperatorException("Could not deserialize resource: " + path, ex);
        }
    }

    public static <T> T deserialize(String path, Class<T> klass, ClassLoader classLoader) {
        try {
            return YAML_MAPPER.readValue(load(path, classLoader), klass);
        }
        catch (JsonProcessingException ex) {
            throw new OperatorException("Could not deserialize resource: " + path, ex);
        }
    }

    public static String load(String path) {
        return load(path, Thread.currentThread().getContextClassLoader());
    }

    public static String load(String path, ClassLoader classLoader) {
        try (var stream = classLoader.getResourceAsStream(path)) {
            return new String(stream.readAllBytes(), Charset.defaultCharset());
        }
        catch (Exception ex) {
            throw new OperatorException("Could not read resource: " + path, ex);
        }
    }
}
