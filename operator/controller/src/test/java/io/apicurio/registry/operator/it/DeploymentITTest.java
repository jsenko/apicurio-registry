package io.apicurio.registry.operator.it;

import io.apicurio.registry.operator.Mapper;
import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@QuarkusTest
public class DeploymentITTest extends ITBase {

    private static final Logger log = LoggerFactory.getLogger(DeploymentITTest.class);

    @Test
    void demoDeployment() {
        // Arrange
        var registry = new ApicurioRegistry3();
        var meta = new ObjectMeta();
        meta.setName("demo");
        meta.setNamespace(getNamespace());
        registry.setMetadata(meta);

        // Act
        client.resources(ApicurioRegistry3.class).inNamespace(getNamespace()).create(registry);

        // Assert
        await().ignoreExceptions().timeout(Duration.ofSeconds(5 * 60)).until(() -> {
            // Debug

            log.debug("================================");

            client.apps().deployments().inNamespace(getNamespace()).list().getItems().forEach(d -> {
                if (d.getMetadata().getName().equals("demo-ui-deployment")) {
                    d.getMetadata().setManagedFields(List.of());
                    log.debug("deployment {}:\n{}", d.getMetadata().getName(), Mapper.toYAML(d));
                    log.debug("ready replicas: {}", d.getStatus().getReadyReplicas());
                }
            });
            client.pods().inNamespace(getNamespace()).list().getItems().forEach(p -> {
                if (p.getMetadata().getName().startsWith("demo-ui-deployment-")) {
                    p.getMetadata().setManagedFields(List.of());
                    log.debug("pod {}:\n{}", p.getMetadata().getName(), Mapper.toYAML(p));
                }
            });

            log.debug("================================");

            client.apps().deployments().inNamespace(getNamespace()).list().getItems().forEach(d -> {
                if (d.getMetadata().getName().equals("demo-app-deployment")) {
                    d.getMetadata().setManagedFields(List.of());
                    log.debug("deployment {}:\n{}", d.getMetadata().getName(), Mapper.toYAML(d));
                    log.debug("ready replicas: {}", d.getStatus().getReadyReplicas());
                }
            });
            client.pods().inNamespace(getNamespace()).list().getItems().forEach(p -> {
                if (p.getMetadata().getName().startsWith("demo-app-deployment-")) {
                    p.getMetadata().setManagedFields(List.of());
                    log.debug("pod {}:\n{}", p.getMetadata().getName(), Mapper.toYAML(p));
                }
            });

            try {
                assertThat(client.apps().deployments().inNamespace(getNamespace())
                        .withName("demo-app-deployment").get().getStatus().getReadyReplicas()).isEqualTo(1);
                assertThat(client.apps().deployments().inNamespace(getNamespace())
                        .withName("demo-ui-deployment").get().getStatus().getReadyReplicas()).isEqualTo(1);
            } catch (Exception ex) {
                log.error("assert error", ex);
                throw ex;
            }
            return true;
        });
    }
}
