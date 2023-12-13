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
import io.apicurio.registry.schema.DocumentBuilderAccessor;
import io.apicurio.registry.schema.compat.RuleViolation;
import io.apicurio.registry.schema.validity.ContentValidator;
import io.apicurio.registry.schema.validity.ValidationResult;
import io.apicurio.registry.schema.validity.ValidityLevel;
import io.apicurio.registry.util.WSDLReaderAccessor;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.Map;

/**
 * @author cfoskin@redhat.com
 */
public class WsdlContentValidator extends XmlContentValidator {

    /**
     * Constructor.
     */
    public WsdlContentValidator() {
    }

    /**
     * @see ContentValidator#validate(ValidityLevel, ContentHandle, Map)
     */
    @Override
    public ValidationResult validate(ValidityLevel level, ContentHandle artifactContent, Map<String, ContentHandle> resolvedReferences) {
        if (level == ValidityLevel.SYNTAX_ONLY || level == ValidityLevel.FULL) {
            try (InputStream stream = artifactContent.stream()) {
                Document wsdlDoc = DocumentBuilderAccessor.getDocumentBuilder().parse(stream);
                if (level == ValidityLevel.FULL) {
                    // validate that its a valid schema
                    WSDLReaderAccessor.getWSDLReader().readWSDL(null, wsdlDoc);
                }
            } catch (Exception ex) {
                return ValidationResult.of(new RuleViolation(ex));
            }
        }
        return ValidationResult.SUCCESS_EMPTY;
    }
}
