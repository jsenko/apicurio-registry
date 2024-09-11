package io.apicurio.registry.operator.resource.ui;

import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.fabric8.kubernetes.api.model.Service;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

import static io.apicurio.registry.operator.resource.ResourceFactory.COMPONENT_UI;
import static io.apicurio.registry.operator.resource.ResourceKey.UI_SERVICE_KEY;

@KubernetesDependent(labelSelector = "app.kubernetes.io/name=apicurio-registry,app.kubernetes.io/component="
        + COMPONENT_UI, resourceDiscriminator = UIServiceDiscriminator.class)
public class UIServiceResource extends CRUDKubernetesDependentResource<Service, ApicurioRegistry3> {

    public UIServiceResource() {
        super(Service.class);
    }

    @Override
    protected Service desired(ApicurioRegistry3 primary, Context<ApicurioRegistry3> context) {
        return UI_SERVICE_KEY.getFactory().apply(primary);
    }
}
