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

import graphql.schema.idl.SchemaParser;
import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.rest.v2.beans.ArtifactReference;
import io.apicurio.registry.schema.compat.RuleViolation;
import io.apicurio.registry.schema.validity.ContentValidator;
import io.apicurio.registry.schema.validity.ValidationResult;
import io.apicurio.registry.schema.validity.ValidityLevel;

import java.util.List;
import java.util.Map;

/**
 * A content validator implementation for the GraphQL content type.
 *
 * @author eric.wittmann@gmail.com
 */
public class GraphQLContentValidator implements ContentValidator {

    /**
     * Constructor.
     */
    public GraphQLContentValidator() {
    }

    /**
     * @see ContentValidator#validate(ValidityLevel, ContentHandle, java.util.Map)
     */
    @Override
    public ValidationResult validate(ValidityLevel level, ContentHandle content, Map<String, ContentHandle> resolvedReferences) {
        if (level == ValidityLevel.SYNTAX_ONLY || level == ValidityLevel.FULL) {
            try {
                new SchemaParser().parse(content.content());
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
