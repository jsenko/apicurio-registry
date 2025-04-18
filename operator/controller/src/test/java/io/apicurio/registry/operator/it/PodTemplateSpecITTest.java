package io.apicurio.registry.operator.it;

import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.apicurio.registry.operator.resource.ResourceFactory;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@QuarkusTest
public class PodTemplateSpecITTest extends ITBase {

    private static final Logger log = LoggerFactory.getLogger(PodTemplateSpecITTest.class);

    @Test
    void testPTS() {
        var exportData = ResourceFactory
                .deserialize("/k8s/examples/podtemplatespec/export-data.configmap.yaml", ConfigMap.class);
        var registry = ResourceFactory.deserialize(
                "/k8s/examples/podtemplatespec/podtemplatespec.apicurioregistry3.yaml",
                ApicurioRegistry3.class);
        registry.getMetadata().setNamespace(namespace);

        client.resource(exportData).create();
        client.resource(registry).create();

        // Deployments
        await().ignoreExceptions().until(() -> {
            assertThat(client.apps().deployments().inNamespace(namespace)
                    .withName(registry.getMetadata().getName() + "-app-deployment").get().getStatus()
                    .getReadyReplicas()).isEqualTo(1);
            assertThat(client.apps().deployments().inNamespace(namespace)
                    .withName(registry.getMetadata().getName() + "-ui-deployment").get().getStatus()
                    .getReadyReplicas()).isEqualTo(1);
            return true;
        });

        // Services
        await().ignoreExceptions().until(() -> {
            assertThat(client.services().inNamespace(namespace)
                    .withName(registry.getMetadata().getName() + "-app-service").get().getSpec()
                    .getClusterIP()).isNotBlank();
            assertThat(client.services().inNamespace(namespace)
                    .withName(registry.getMetadata().getName() + "-ui-service").get().getSpec()
                    .getClusterIP()).isNotBlank();
            return true;
        });

        int appServicePort = portForwardManager
                .startPortForward(registry.getMetadata().getName() + "-app-service", 8080);

        await().ignoreExceptions().until(() -> {
            given().get(new URI("http://localhost:" + appServicePort
                    + "/apis/registry/v3/groups/test/artifacts/test/versions/1")).then().statusCode(200);
            return true;
        });
    }
}
