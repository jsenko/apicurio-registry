package io.apicurio.registry.storage;

import io.apicurio.common.apps.logging.LoggerProducer;
import io.apicurio.registry.schema.ArtifactTypeUtilProviderFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class RegistryStorageContentUtilsProducer {

    @Inject
    LoggerProducer loggerProducer;

    @Inject
    ArtifactTypeUtilProviderFactory atupf;


    @Produces
    @ApplicationScoped
    public RegistryStorageContentUtils produce() {
        return new RegistryStorageContentUtils(loggerProducer.getLogger(RegistryStorageContentUtils.class), atupf);
    }
}
