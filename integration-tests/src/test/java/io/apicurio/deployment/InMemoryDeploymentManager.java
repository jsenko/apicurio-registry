package io.apicurio.deployment;


import static io.apicurio.deployment.KubernetesTestResources.APPLICATION_IN_MEMORY_ICEBERG_RESOURCES;
import static io.apicurio.deployment.KubernetesTestResources.APPLICATION_IN_MEMORY_RESOURCES;
import static io.apicurio.deployment.KubernetesTestResources.APPLICATION_IN_MEMORY_SECURED_RESOURCES;
import static io.apicurio.deployment.RegistryDeploymentManager.prepareTestsInfra;

public class InMemoryDeploymentManager {

    static void deployInMemoryApp(String registryImage) throws Exception {
        if (TestGroups.isAnyGroupActive(TestGroups.AUTH)) {
            prepareTestsInfra(null, APPLICATION_IN_MEMORY_SECURED_RESOURCES, true, registryImage);
        } else if (TestGroups.isAnyGroupActive(TestGroups.ICEBERG)) {
            prepareTestsInfra(null, APPLICATION_IN_MEMORY_ICEBERG_RESOURCES, false, registryImage);
        } else {
            prepareTestsInfra(null, APPLICATION_IN_MEMORY_RESOURCES, false, registryImage);
        }
    }
}
