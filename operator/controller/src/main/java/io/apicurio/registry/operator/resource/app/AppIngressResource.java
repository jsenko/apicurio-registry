package io.apicurio.registry.operator.resource.app;

import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.apicurio.registry.operator.util.HostUtil;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.quarkus.arc.Arc;

import static io.apicurio.registry.operator.resource.ResourceFactory.COMPONENT_APP;
import static io.apicurio.registry.operator.resource.ResourceKey.APP_INGRESS_KEY;
import static io.apicurio.registry.operator.resource.ResourceKey.APP_SERVICE_KEY;

@KubernetesDependent(labelSelector = "app.kubernetes.io/name=apicurio-registry,app.kubernetes.io/component="
        + COMPONENT_APP, resourceDiscriminator = AppIngressDiscriminator.class)
public class AppIngressResource extends CRUDKubernetesDependentResource<Ingress, ApicurioRegistry3> {

    public AppIngressResource() {
        super(Ingress.class);
    }

    @Override
    protected Ingress desired(ApicurioRegistry3 primary, Context<ApicurioRegistry3> context) {

        var hostUtil = Arc.container().instance(HostUtil.class).get();

        var i = APP_INGRESS_KEY.getFactory().apply(primary);

        var sOpt = context.getSecondaryResource(APP_SERVICE_KEY.getKlass(),
                APP_SERVICE_KEY.getDiscriminator());
        sOpt.ifPresent(s -> {
            for (IngressRule rule : i.getSpec().getRules()) {
                for (HTTPIngressPath path : rule.getHttp().getPaths()) {
                    if (s.getMetadata().getName().equals(path.getBackend().getService().getName())) {
                        rule.setHost(hostUtil.getHost(COMPONENT_APP, primary));
                        return;
                    }
                }
            }
        });

        return i;
    }
}
