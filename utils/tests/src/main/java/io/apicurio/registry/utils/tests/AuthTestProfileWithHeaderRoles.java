package io.apicurio.registry.utils.tests;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AuthTestProfileWithHeaderRoles implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("apicurio.auth.role-source", "header");
    }

    @Override
    public List<QuarkusTestProfile.TestResourceEntry> testResources() {
        return Collections
                .singletonList(new QuarkusTestProfile.TestResourceEntry(KeycloakTestContainerManager.class));
    }
}
