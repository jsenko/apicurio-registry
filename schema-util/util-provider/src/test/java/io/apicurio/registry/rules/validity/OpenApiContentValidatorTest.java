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

package io.apicurio.registry.rules.validity;

import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.rest.v2.beans.ArtifactReference;
import io.apicurio.registry.schema.validity.ValidityLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tests the OpenAPI content validator.
 * @author eric.wittmann@gmail.com
 */
public class OpenApiContentValidatorTest extends ArtifactUtilProviderTestBase {

    @Test
    public void testValidSyntax() {
        ContentHandle content = resourceToContentHandle("openapi-valid-syntax.json");
        OpenApiContentValidator validator = new OpenApiContentValidator();
        Assertions.assertTrue(validator.validate(ValidityLevel.SYNTAX_ONLY, content, Collections.emptyMap()).isValid());
    }


    @Test
    public void testValidSyntax_OpenApi31() {
        ContentHandle content = resourceToContentHandle("openapi-valid-syntax-openapi31.json");
        OpenApiContentValidator validator = new OpenApiContentValidator();
        Assertions.assertTrue(validator.validate(ValidityLevel.SYNTAX_ONLY, content, Collections.emptyMap()).isValid());
    }

    @Test
    public void testValidSemantics() {
        ContentHandle content = resourceToContentHandle("openapi-valid-semantics.json");
        OpenApiContentValidator validator = new OpenApiContentValidator();
        Assertions.assertTrue(validator.validate(ValidityLevel.FULL, content, Collections.emptyMap()).isValid());
    }

    @Test
    public void testInvalidSyntax() {
        ContentHandle content = resourceToContentHandle("openapi-invalid-syntax.json");
        OpenApiContentValidator validator = new OpenApiContentValidator();
        Assertions.assertFalse(validator.validate(ValidityLevel.SYNTAX_ONLY, content, Collections.emptyMap()).isValid());
    }

    @Test
    public void testInvalidSemantics() {
        ContentHandle content = resourceToContentHandle("openapi-invalid-semantics.json");
        OpenApiContentValidator validator = new OpenApiContentValidator();
        Assertions.assertFalse(validator.validate(ValidityLevel.FULL, content, Collections.emptyMap()).isValid());
    }

    @Test
    public void testValidateRefs() {
        ContentHandle content = resourceToContentHandle("openapi-valid-with-refs.json");
        OpenApiContentValidator validator = new OpenApiContentValidator();
        Assertions.assertTrue(validator.validate(ValidityLevel.SYNTAX_ONLY, content, Collections.emptyMap()).isValid());

        // Properly map both required references - success.
        {
            List<ArtifactReference> references = new ArrayList<>();
            references.add(ArtifactReference.builder()
                    .groupId("default")
                    .artifactId("ExternalWidget")
                    .version("1.0")
                    .name("example.com#/components/schemas/ExternalWidget").build());
            references.add(ArtifactReference.builder()
                    .groupId("default")
                    .artifactId("AnotherWidget")
                    .version("1.1")
                    .name("example.com#/components/schemas/AnotherWidget").build());
            validator.validateReferences(content, references);
        }

        // Don't map either of the required references - failure.
        List<ArtifactReference> references = new ArrayList<>();
        Assertions.assertFalse(validator.validateReferences(content, references).isValid());

        // Only map one of the two required refs - failure.
        references = new ArrayList<>();
        references.add(ArtifactReference.builder()
                .groupId("default")
                .artifactId("AnotherWidget")
                .version("1.1")
                .name("example.com#/components/schemas/AnotherWidget").build());
        Assertions.assertFalse(validator.validateReferences(content, references).isValid());

        // Only map one of the two required refs - failure.
        references = new ArrayList<>();
        references.add(ArtifactReference.builder()
                .groupId("default")
                .artifactId("AnotherWidget")
                .version("1.1")
                .name("example.com#/components/schemas/AnotherWidget").build());
        references.add(ArtifactReference.builder()
                .groupId("default")
                .artifactId("WrongWidget")
                .version("2.3")
                .name("example.com#/components/schemas/WrongWidget").build());
        Assertions.assertFalse(validator.validateReferences(content, references).isValid());
    }
}
