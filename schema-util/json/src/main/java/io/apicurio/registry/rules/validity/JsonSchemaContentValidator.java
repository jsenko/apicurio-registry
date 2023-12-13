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


import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.rest.v2.beans.ArtifactReference;
import io.apicurio.registry.rules.compatibility.jsonschema.JsonUtil;
import io.apicurio.registry.schema.compat.RuleViolation;
import io.apicurio.registry.schema.validity.ContentValidator;
import io.apicurio.registry.schema.validity.ValidationResult;
import io.apicurio.registry.schema.validity.ValidityLevel;
import org.everit.json.schema.SchemaException;

import java.util.List;
import java.util.Map;

/**
 * A content validator implementation for the JsonSchema content type.
 *
 * @author eric.wittmann@gmail.com
 */
public class JsonSchemaContentValidator implements ContentValidator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor.
     */
    public JsonSchemaContentValidator() {
    }

    /**
     * @see ContentValidator#validate(ValidityLevel, ContentHandle, Map)
     */
    @Override
    public ValidationResult validate(ValidityLevel level, ContentHandle artifactContent, Map<String, ContentHandle> resolvedReferences) {
        if (level == ValidityLevel.SYNTAX_ONLY) {
            try {
                objectMapper.readTree(artifactContent.bytes());
            } catch (Exception ex) {
                return ValidationResult.of(new RuleViolation(ex));
            }
        } else if (level == ValidityLevel.FULL) {
            try {
                JsonUtil.readSchema(artifactContent.content(), resolvedReferences);
            } catch (SchemaException e) {
                String context = e.getSchemaLocation();
                String description = e.getMessage();
                if (description != null && description.contains(":")) {
                    description = description.substring(description.indexOf(":") + 1).trim();
                }
                return ValidationResult.of(new RuleViolation(description, context));
            } catch (Exception ex) {
                return ValidationResult.of(new RuleViolation(ex));
            }
        }
        return ValidationResult.SUCCESS_EMPTY;
    }

    /**
     * @see ContentValidator#validateReferences(ContentHandle, java.util.List)
     */
    @Override
    public ValidationResult validateReferences(ContentHandle artifactContent, List<ArtifactReference> references) {
        // TODO Implement this, or throw new UnsupportedOperationException();
        return ValidationResult.SUCCESS_EMPTY;
    }
}
