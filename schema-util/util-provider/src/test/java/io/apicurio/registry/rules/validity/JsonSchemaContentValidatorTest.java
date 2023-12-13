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
 * Tests the JSON Schema content validator.
 * @author eric.wittmann@gmail.com
 */
public class JsonSchemaContentValidatorTest extends ArtifactUtilProviderTestBase {

    @Test
    public void testValidJsonSchema() {
        ContentHandle content = resourceToContentHandle("jsonschema-valid.json");
        JsonSchemaContentValidator validator = new JsonSchemaContentValidator();
        Assertions.assertTrue(validator.validate(ValidityLevel.SYNTAX_ONLY, content, Collections.emptyMap()).isValid());
    }

    @Test
    public void testInvalidJsonSchema() {
        ContentHandle content = resourceToContentHandle("jsonschema-invalid.json");
        JsonSchemaContentValidator validator = new JsonSchemaContentValidator();
        Assertions.assertFalse(validator.validate(ValidityLevel.SYNTAX_ONLY, content, Collections.emptyMap()).isValid());
    }

    @Test
    public void testInvalidJsonSchemaVersion() {
        ContentHandle content = resourceToContentHandle("jsonschema-valid-d7.json");
        JsonSchemaContentValidator validator = new JsonSchemaContentValidator();
        Assertions.assertTrue(validator.validate(ValidityLevel.FULL, content, Collections.emptyMap()).isValid());
    }

    @Test
    public void testInvalidJsonSchemaFull() {
        ContentHandle content = resourceToContentHandle("bad-json-schema-v1.json");
        JsonSchemaContentValidator validator = new JsonSchemaContentValidator();
        var result = validator.validate(ValidityLevel.FULL, content, Collections.emptyMap());
        Assertions.assertFalse(result.isValid());
        Assertions.assertEquals("expected type: Number, found: Boolean", result.getViolations().iterator().next().getDescription());
        Assertions.assertEquals("#/items/properties/price/exclusiveMinimum", result.getViolations().iterator().next().getContext());
    }

    @Test
    public void testJsonSchemaWithReferences() {
        ContentHandle city = resourceToContentHandle("city.json");
        ContentHandle citizen = resourceToContentHandle("citizen.json");
        JsonSchemaContentValidator validator = new JsonSchemaContentValidator();
        Assertions.assertTrue(validator.validate(ValidityLevel.FULL, citizen, Collections.singletonMap("https://example.com/city.json", city)).isValid());
    }
}
