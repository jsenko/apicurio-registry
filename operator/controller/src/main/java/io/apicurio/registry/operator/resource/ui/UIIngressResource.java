package io.apicurio.registry.operator.resource.ui;

import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.apicurio.registry.operator.util.HostUtil;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.quarkus.arc.Arc;

import static io.apicurio.registry.operator.resource.ResourceFactory.COMPONENT_UI;
import static io.apicurio.registry.operator.resource.ResourceKey.UI_INGRESS_KEY;
import static io.apicurio.registry.operator.resource.ResourceKey.UI_SERVICE_KEY;

@KubernetesDependent(labelSelector = "app.kubernetes.io/name=apicurio-registry,app.kubernetes.io/component="
        + COMPONENT_UI, resourceDiscriminator = UIIngressDiscriminator.class)
public class UIIngressResource extends CRUDKubernetesDependentResource<Ingress, ApicurioRegistry3> {

    public UIIngressResource() {
        super(Ingress.class);
    }

    @Override
    protected Ingress desired(ApicurioRegistry3 primary, Context<ApicurioRegistry3> context) {

        var hostUtil = Arc.container().instance(HostUtil.class).get();

        var i = UI_INGRESS_KEY.getFactory().apply(primary);

        var sOpt = context.getSecondaryResource(UI_SERVICE_KEY.getKlass(), UI_SERVICE_KEY.getDiscriminator());
        sOpt.ifPresent(s -> {
            for (IngressRule rule : i.getSpec().getRules()) {
                for (HTTPIngressPath path : rule.getHttp().getPaths()) {
                    if (s.getMetadata().getName().equals(path.getBackend().getService().getName())) {
                        rule.setHost(hostUtil.getHost(COMPONENT_UI, primary));
                        return;
                    }
                }
            }
        });

        return i;
    }
}
