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

import com.google.protobuf.Descriptors;
import com.squareup.wire.schema.internal.parser.MessageElement;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.rest.v2.beans.ArtifactReference;
import io.apicurio.registry.schema.compat.RuleViolation;
import io.apicurio.registry.schema.validity.ContentValidator;
import io.apicurio.registry.schema.validity.ValidationResult;
import io.apicurio.registry.schema.validity.ValidityLevel;
import io.apicurio.registry.utils.protobuf.schema.FileDescriptorUtils;
import io.apicurio.registry.utils.protobuf.schema.ProtobufFile;
import io.apicurio.registry.utils.protobuf.schema.ProtobufSchema;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A content validator implementation for the Protobuf content type.
 *
 * @author eric.wittmann@gmail.com
 */
public class ProtobufContentValidator implements ContentValidator {

    /**
     * Constructor.
     */
    public ProtobufContentValidator() {
    }

    /**
     * @see ContentValidator#validate(ValidityLevel, ContentHandle, Map)
     */
    @Override
    public ValidationResult validate(ValidityLevel level, ContentHandle artifactContent, Map<String, ContentHandle> resolvedReferences) {
        if (level == ValidityLevel.SYNTAX_ONLY || level == ValidityLevel.FULL) {
            try {
                if (resolvedReferences == null || resolvedReferences.isEmpty()) {
                    ProtobufFile.toProtoFileElement(artifactContent.content());
                } else {
                    final ProtoFileElement protoFileElement = ProtobufFile.toProtoFileElement(artifactContent.content());
                    final Map<String, ProtoFileElement> dependencies = Collections.unmodifiableMap(resolvedReferences.entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> ProtobufFile.toProtoFileElement(e.getValue().content())
                            )));
                    MessageElement firstMessage = FileDescriptorUtils.firstMessage(protoFileElement);
                    if (firstMessage != null) {
                        try {
                            final Descriptors.Descriptor fileDescriptor = FileDescriptorUtils.toDescriptor(firstMessage.getName(), protoFileElement, dependencies);
                            ContentHandle.create(fileDescriptor.toString());
                        } catch (IllegalStateException ise) {
                            //If we fail to init the dynamic schema, try to get the descriptor from the proto element
                            ContentHandle.create(getFileDescriptorFromElement(protoFileElement).toString());
                        }
                    } else {
                        ContentHandle.create(getFileDescriptorFromElement(protoFileElement).toString());
                    }
                }
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
        try {
            Set<String> mappedRefs = references.stream().map(ref -> ref.getName()).collect(Collectors.toSet());

            ProtoFileElement protoFileElement = ProtobufFile.toProtoFileElement(artifactContent.content());
            Set<String> allImports = new HashSet<>();
            allImports.addAll(protoFileElement.getImports());
            allImports.addAll(protoFileElement.getPublicImports());

            Set<RuleViolation> violations = allImports.stream().filter(_import -> !mappedRefs.contains(_import)).map(missingRef -> {
                return new RuleViolation("Unmapped reference detected.", missingRef);
            }).collect(Collectors.toSet());

            return ValidationResult.builder()
                    .violations(violations)
                    .build();
        } catch (Exception e) {
            // Do nothing - we don't care if it can't validate.  Another rule will handle that.
            return ValidationResult.SUCCESS_EMPTY;
        }
    }

    private ProtobufSchema getFileDescriptorFromElement(ProtoFileElement fileElem) throws Descriptors.DescriptorValidationException {
        Descriptors.FileDescriptor fileDescriptor = FileDescriptorUtils.protoFileToFileDescriptor(fileElem);
        return new ProtobufSchema(fileDescriptor, fileElem);
    }
}
