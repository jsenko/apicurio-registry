package io.apicurio.registry.operator.resource.ui;

import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicy;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyIngressRuleBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.apicurio.registry.operator.resource.ResourceKey.UI_NETWORK_POLICY_KEY;
import static io.apicurio.registry.operator.utils.Mapper.toYAML;

@KubernetesDependent
public class UINetworkPolicyResource
        extends CRUDKubernetesDependentResource<NetworkPolicy, ApicurioRegistry3> {

    private static final Logger log = LoggerFactory.getLogger(UINetworkPolicyResource.class);

    public UINetworkPolicyResource() {
        super(NetworkPolicy.class);
    }

    @Override
    protected NetworkPolicy desired(ApicurioRegistry3 primary, Context<ApicurioRegistry3> context) {
        var networkPolicy = UI_NETWORK_POLICY_KEY.getFactory().apply(primary);

        // @formatter:off
        networkPolicy.getSpec().getIngress().add(new NetworkPolicyIngressRuleBuilder()
                    .addNewPort()
                        .withProtocol("TCP")
                        .withPort(new IntOrString(8080))
                    .endPort()
                .build());
        // @formatter:on

        log.trace("Desired {} is {}", UI_NETWORK_POLICY_KEY.getId(), toYAML(networkPolicy));
        return networkPolicy;
    }
}
