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

package io.apicurio.registry.types.provider;

import io.apicurio.registry.schema.canon.ContentCanonicalizer;
import io.apicurio.registry.content.canon.KafkaConnectContentCanonicalizer;
import io.apicurio.registry.schema.deref.ContentDereferencer;
import io.apicurio.registry.schema.extractor.ContentExtractor;
import io.apicurio.registry.types.provider.noop.NoopContentExtractor;
import io.apicurio.registry.types.provider.noop.NoOpReferenceFinder;
import io.apicurio.registry.schema.refs.ReferenceFinder;
import io.apicurio.registry.schema.compat.CompatibilityChecker;
import io.apicurio.registry.types.provider.noop.NoopCompatibilityChecker;
import io.apicurio.registry.schema.validity.ContentValidator;
import io.apicurio.registry.rules.validity.KafkaConnectContentValidator;
import io.apicurio.registry.types.ArtifactType;

/**
 * @author Ales Justin
 */
public class KConnectArtifactTypeUtilProvider extends AbstractArtifactTypeUtilProvider {
    @Override
    public String getArtifactType() {
        return ArtifactType.KCONNECT;
    }

    @Override
    protected CompatibilityChecker createCompatibilityChecker() {
        return NoopCompatibilityChecker.INSTANCE;
    }

    @Override
    protected ContentCanonicalizer createContentCanonicalizer() {
        return new KafkaConnectContentCanonicalizer();
    }

    @Override
    protected ContentValidator createContentValidator() {
        return new KafkaConnectContentValidator();
    }

    @Override
    protected ContentExtractor createContentExtractor() {
        return NoopContentExtractor.INSTANCE;
    }

    @Override
    public ContentDereferencer getContentDereferencer() {
        return null;
    }
    
    @Override
    public ReferenceFinder getReferenceFinder() {
        return NoOpReferenceFinder.INSTANCE;
    }
}
