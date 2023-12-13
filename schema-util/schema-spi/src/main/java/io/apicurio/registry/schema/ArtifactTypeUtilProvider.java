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

package io.apicurio.registry.schema;

import io.apicurio.registry.schema.canon.ContentCanonicalizer;
import io.apicurio.registry.schema.compat.CompatibilityChecker;
import io.apicurio.registry.schema.deref.ContentDereferencer;
import io.apicurio.registry.schema.refs.ReferenceFinder;
import io.apicurio.registry.schema.validity.ContentValidator;
import io.apicurio.registry.schema.extractor.ContentExtractor;

/**
 * Interface providing different utils per artifact type
 * * compatibility checker
 * * content canonicalizer
 * * content validator
 * * rules
 * * etc ...
 *
 * @author Ales Justin
 */
public interface ArtifactTypeUtilProvider {
    String getArtifactType();

    CompatibilityChecker getCompatibilityChecker();

    ContentCanonicalizer getContentCanonicalizer();

    ContentValidator getContentValidator();

    ContentExtractor getContentExtractor();

    ContentDereferencer getContentDereferencer();
    
    ReferenceFinder getReferenceFinder();
}
