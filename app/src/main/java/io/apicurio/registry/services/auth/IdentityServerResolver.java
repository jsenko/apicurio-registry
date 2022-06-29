/*
 * Copyright 2022 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.registry.services.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import io.apicurio.rest.client.JdkHttpClientProvider;
import io.apicurio.rest.client.auth.exception.AuthErrorHandler;
import io.apicurio.rest.client.request.Operation;
import io.apicurio.rest.client.request.Request;
import io.apicurio.rest.client.spi.ApicurioHttpClient;
import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.TenantConfigResolver;
import io.quarkus.oidc.runtime.TenantConfigBean;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;

@ApplicationScoped
public class IdentityServerResolver implements TenantConfigResolver {

    @ConfigProperty(name = "registry.identity.server.resolver.enabled")
    Boolean resolveIdentityServer;

    @ConfigProperty(name = "registry.identity.server.resolver.request-base-path")
    String resolverRequestBasePath;

    @ConfigProperty(name = "registry.identity.server.resolver.request-path")
    String resolverRequestPath;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String apiClientId;

    private ApicurioHttpClient httpClient;

    @Inject
    TenantConfigBean tenantConfigBean;

    public static String OIDC_DYNAMIC_TENANT_ID = "DYNAMIC_TENANT_ID";

    @PostConstruct
    public void init() {
        if (resolveIdentityServer) {
            httpClient = new JdkHttpClientProvider().create(resolverRequestBasePath, Collections.emptyMap(), null, new AuthErrorHandler());
        }
    }

    private OidcTenantConfig resolveIdentityServer() {
        final SsoProviders ssoProviders = httpClient.sendRequest(getSSOProviders(resolverRequestPath));
        final OidcTenantConfig config = new OidcTenantConfig();

        config.setTenantId(OIDC_DYNAMIC_TENANT_ID);
        config.setTokenPath(ssoProviders.getTokenUrl());
        config.setJwksPath(ssoProviders.getJwks());
        config.setAuthServerUrl(ssoProviders.getValidIssuer());
        config.setClientId(apiClientId);

        return  config;
    }

    public static Request<SsoProviders> getSSOProviders(String resolverRequestPath) {
        return new Request.RequestBuilder<SsoProviders>()
                .operation(Operation.GET)
                .path(resolverRequestPath)
                .responseType(new TypeReference<SsoProviders>() {
                })
                .build();
    }

    @Override
    public Uni<OidcTenantConfig> resolve(RoutingContext routingContext, OidcRequestContext<OidcTenantConfig> requestContext) {
        if (resolveIdentityServer) {
            return Uni.createFrom().item(resolveIdentityServer());
        }

        //resolve to default configuration
        return Uni.createFrom().item(tenantConfigBean.getDefaultTenant().getOidcTenantConfig());
    }

    public static class SsoProviders {

        @JsonProperty("base_url")
        private String baseUrl;
        @JsonProperty("token_url")
        private String tokenUrl;
        @JsonProperty("jwks")
        private String jwks;
        @JsonProperty("valid_issuer")
        private String validIssuer;

        public SsoProviders() {
        }

        public SsoProviders(String baseUrl, String tokenUrl, String jwks, String validIssuer) {
            this.baseUrl = baseUrl;
            this.tokenUrl = tokenUrl;
            this.jwks = jwks;
            this.validIssuer = validIssuer;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        public void setJwks(String jwks) {
            this.jwks = jwks;
        }

        public void setValidIssuer(String validIssuer) {
            this.validIssuer = validIssuer;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getTokenUrl() {
            return tokenUrl;
        }

        public String getJwks() {
            return jwks;
        }

        public String getValidIssuer() {
            return validIssuer;
        }
    }
}
