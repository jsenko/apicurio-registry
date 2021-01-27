/*
 * Copyright 2020 Red Hat
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

package io.apicurio.registry.utils.serde.strategy;

import io.apicurio.registry.client.RegistryRestClient;
import io.apicurio.registry.client.exception.ArtifactNotFoundException;
import io.apicurio.registry.client.exception.RestClientException;
import io.apicurio.registry.rest.v1.beans.ArtifactMetaData;
import io.apicurio.registry.rest.v1.beans.IfExistsType;
import io.apicurio.registry.types.ArtifactType;
import io.apicurio.registry.utils.serde.SchemaCache;

import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

/**
 * @author Ales Justin
 */
public abstract class AbstractCrudIdStrategy<T> extends CheckPeriodIdStrategy<T> {

    protected boolean isNotFound(Response response) {
        return response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND;
    }

    protected abstract long initialLookup(RegistryRestClient service, String artifactId, ArtifactType artifactType, T schema, SchemaCache<T> cache);

    protected void afterCreateArtifact(T schema, ArtifactMetaData amd, SchemaCache<T> cache) {
        populateCache(schema, amd.getGlobalId(), cache);
    }
    
    /**
     * @see io.apicurio.registry.utils.serde.strategy.CheckPeriodIdStrategy#findIdInternal(io.apicurio.registry.client.RegistryRestClient, java.lang.String, io.apicurio.registry.types.ArtifactType, java.lang.Object, SchemaCache)
     */
    @Override
    long findIdInternal(RegistryRestClient client, String artifactId, ArtifactType artifactType, T schema, SchemaCache<T> cache) {
        try {
            return initialLookup(client, artifactId, artifactType, schema, cache);
        } catch (ArtifactNotFoundException e) {
            // TODO add an option to search by strict content?
            ArtifactMetaData amd = client.createArtifact(artifactId, artifactType, toStream(schema), IfExistsType.RETURN_OR_UPDATE, true);
            afterCreateArtifact(schema, amd, cache);
            return amd.getGlobalId();
        } catch (RestClientException e) {
            throw new IllegalStateException(String.format(
                    "Error [%s] retrieving schema: %s",
                    e.getMessage(),
                    artifactId)
            );
        }
    }
}
