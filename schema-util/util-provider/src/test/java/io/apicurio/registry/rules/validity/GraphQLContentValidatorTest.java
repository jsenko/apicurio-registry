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
import io.apicurio.registry.schema.validity.ValidityLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * Tests the GraphQL content validator.
 * @author eric.wittmann@gmail.com
 */
public class GraphQLContentValidatorTest extends ArtifactUtilProviderTestBase {

    @Test
    public void testValidSyntax() {
        ContentHandle content = resourceToContentHandle("graphql-valid.graphql");
        GraphQLContentValidator validator = new GraphQLContentValidator();
        Assertions.assertTrue(validator.validate(ValidityLevel.SYNTAX_ONLY, content, Collections.emptyMap()).isValid());
    }

    @Test
    public void testInvalidSyntax() {
        ContentHandle content = resourceToContentHandle("graphql-invalid.graphql");
        GraphQLContentValidator validator = new GraphQLContentValidator();
        Assertions.assertFalse(validator.validate(ValidityLevel.SYNTAX_ONLY, content, Collections.emptyMap()).isValid());
    }
}
