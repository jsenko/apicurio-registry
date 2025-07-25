package io.apicurio.registry.operator;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.api.config.ConfigurationServiceOverrider;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static io.quarkus.runtime.configuration.ConfigUtils.isProfileActive;
import static org.eclipse.microprofile.config.ConfigProvider.getConfig;

@ApplicationScoped
public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    @Inject
    Instance<Reconciler<? extends HasMetadata>> reconcilers;

    @Inject
    KubernetesClient client;

    private Operator operator;

    @Startup
    void startup() {
        if (isProfileActive("test")) {
            log.info("Operator is not started automatically during testing.");
        } else {
            start();
        }
    }

    public void start() {
        start(configOverride -> {
            configOverride.withKubernetesClient(client);
            configOverride.withUseSSAToPatchPrimaryResource(false);
        });
    }

    public void start(Consumer<ConfigurationServiceOverrider> configOverride) {
        log.info("Starting the Apicurio Registry 3 Operator version {} .", getConfig().getValue("registry.version", String.class));
        operator = new Operator(configOverride);
        reconcilers.forEach(operator::register);
        operator.start();
        log.info("Operator started");
    }

    @Shutdown
    void shutdown() {
        if (isProfileActive("test")) {
            log.info("Operator is not stopped automatically during testing.");
        } else {
            stop();
        }
    }

    public void stop() {
        log.info("Stopping the Apicurio Registry 3 Operator.");
        operator.stop();
    }

    @Produces
    @Priority(100)
    public Operator produce() {
        return operator;
    }
}
