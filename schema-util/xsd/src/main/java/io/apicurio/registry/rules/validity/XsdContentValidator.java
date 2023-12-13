/*
 * Copyright 2020 Red Hat Inc
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
import io.apicurio.registry.schema.compat.RuleViolation;
import io.apicurio.registry.schema.validity.ContentValidator;
import io.apicurio.registry.schema.validity.ValidationResult;
import io.apicurio.registry.schema.validity.ValidityLevel;
import io.apicurio.registry.util.SchemaFactoryAccessor;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.util.Map;

/**
 * @author cfoskin@redhat.com
 */
public class XsdContentValidator extends XmlContentValidator {

    /**
     * Constructor.
     */
    public XsdContentValidator() {
    }

    /**
     * @see ContentValidator#validate(ValidityLevel, ContentHandle, Map)
     */
    @Override
    public ValidationResult validate(ValidityLevel level, ContentHandle artifactContent, Map<String, ContentHandle> resolvedReferences) {
        var result = super.validate(level, artifactContent, resolvedReferences);

        if (level == ValidityLevel.FULL) {
            try (InputStream semanticStream = artifactContent.stream()) {
                // validate that its a valid schema
                Source source = new StreamSource(semanticStream);
                SchemaFactoryAccessor.getSchemaFactory().newSchema(source);
            } catch (Exception ex) {
                result = result.merge(ValidationResult.of(new RuleViolation(ex)));
            }
        }
        return result;
    }
}
