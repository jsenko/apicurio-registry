package io.apicurio.registry.operator.util;

import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.apicurio.registry.operator.state.impl.ClusterInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class HostUtil {

    private static final Logger log = LoggerFactory.getLogger(HostUtil.class);

    @Inject
    ClusterInfo clusterInfo;

    public String getHost(String component, ApicurioRegistry3 p) {
        var prefix = p.getMetadata().getName() + "-" + component + "." + p.getMetadata().getNamespace();
        String host;
        if (clusterInfo.getCanonicalHost().isAvailable()) {
            host = prefix + "." + clusterInfo.getCanonicalHost().getValue();
        } else {
            host = prefix + ".cluster.example";
        }
        // log.info("component = {}, ost = {}, clusterInfo = {}", component, host, clusterInfo);
        return host;
    }
}
