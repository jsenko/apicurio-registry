package io.apicurio.registry.util;

import io.apicurio.registry.schema.ArtifactTypeUtilProviderFactory;
import io.apicurio.registry.types.provider.ArtifactTypeUtilProviderFactoryImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

public class ArtifactTypeUtilProviderFactoryProducer {


    @Produces
    @ApplicationScoped
    ArtifactTypeUtilProviderFactory produce() {
        return new ArtifactTypeUtilProviderFactoryImpl();
    }
}
