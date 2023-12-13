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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.rest.v2.beans.ArtifactReference;
import io.apicurio.registry.schema.compat.RuleViolation;
import io.apicurio.registry.schema.validity.ContentValidator;
import io.apicurio.registry.schema.validity.ValidationResult;
import io.apicurio.registry.schema.validity.ValidityLevel;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.json.JsonConverterConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A content validator implementation for the Kafka Connect schema content type.
 *
 * @author eric.wittmann@gmail.com
 */
public class KafkaConnectContentValidator implements ContentValidator {

    private static final ObjectMapper mapper;
    private static final JsonConverter jsonConverter;

    static {
        mapper = new ObjectMapper();
        jsonConverter = new JsonConverter();
        Map<String, Object> configs = new HashMap<>();
        configs.put("converter.type", "key");
        configs.put(JsonConverterConfig.SCHEMAS_CACHE_SIZE_CONFIG, 0);
        jsonConverter.configure(configs);
    }

    /**
     * Constructor.
     */
    public KafkaConnectContentValidator() {
    }

    /**
     * @see ContentValidator#validate(ValidityLevel, ContentHandle, Map)
     */
    @Override
    public ValidationResult validate(ValidityLevel level, ContentHandle artifactContent, Map<String, ContentHandle> resolvedReferences) {
        if (level == ValidityLevel.SYNTAX_ONLY || level == ValidityLevel.FULL) {
            try {
                JsonNode jsonNode = mapper.readTree(artifactContent.content());
                jsonConverter.asConnectSchema(jsonNode);
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
