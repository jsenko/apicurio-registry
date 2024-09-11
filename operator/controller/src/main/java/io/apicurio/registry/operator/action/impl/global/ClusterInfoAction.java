package io.apicurio.registry.operator.action.impl.global;

import io.apicurio.registry.operator.state.impl.ClusterInfo;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteIngress;
import io.fabric8.openshift.client.OpenShiftClient;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

import static io.apicurio.registry.operator.util.MaybeAvailable.available;
import static io.apicurio.registry.operator.util.MaybeAvailable.notAvailable;

@ApplicationScoped
public class ClusterInfoAction {

    private static final Logger log = LoggerFactory.getLogger(ClusterInfoAction.class);

    @Inject
    KubernetesClient kc;

    @Inject
    ClusterInfo clusterInfo;

    @Scheduled(every = "10s")
    public void run() {

        var oc = kc.adapt(OpenShiftClient.class);

        if (clusterInfo.getIsOCP().isUnknown()) {
            clusterInfo.setIsOCP(available(oc.isSupported()));
        }

        if (clusterInfo.getCanonicalHost().isUnknown()) {
            clusterInfo.getIsOCP().ifAvailable(isOCP -> {
                if (isOCP) {
                    // spotless:off
                    // TODO: This caused an exception during testing:
                    // WARN [null] (vertx-blocked-thread-checker) Thread Thread[vert.x-eventloop-thread-1,5,main] has been blocked for 3497 ms, time limit is 2000 ms: io.vertx.core.VertxException: Thread blocked
                    // TODO: Avoiding search in all namespaces.
                    // In the meantime, the time limit has been increased to 10s by:
                    // quarkus.vertx.max-event-loop-execute-time=10s
                    // spotless:on
                    var routes = oc.routes().inAnyNamespace()
                            .withLabel("app.kubernetes.io/name", "apicurio-registry").list();

                    if (routes.getItems().size() > 0) {
                        var canonicalHosts = new HashSet<String>();
                        for (Route route : routes.getItems()) {
                            for (RouteIngress ingress : route.getStatus().getIngress()) {
                                if (ingress.getRouterCanonicalHostname() != null
                                        && !ingress.getRouterCanonicalHostname().isEmpty()) {
                                    canonicalHosts.add(ingress.getRouterCanonicalHostname());
                                }
                            }
                        }
                        if (canonicalHosts.size() == 0) {
                            log.warn("Could not find any canonical hosts.");
                            clusterInfo.setCanonicalHost(notAvailable());
                        } else {
                            if (canonicalHosts.size() > 1) {
                                log.warn("There are multiple canonical hosts: {}. Selecting randomly.",
                                        canonicalHosts);
                            }
                            clusterInfo.setCanonicalHost(available(canonicalHosts.iterator().next()));
                        }
                    }

                } else {
                    clusterInfo.setCanonicalHost(notAvailable());
                }
            });
        }
    }
}
