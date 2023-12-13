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

package io.apicurio.registry.rules;

import io.apicurio.registry.schema.compat.RuleViolation;
import io.apicurio.registry.types.RegistryException;
import io.apicurio.registry.types.RuleType;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * Exception thrown when a configured rule is violated, rejecting an artifact content
 * update.
 *
 * @author Ales Justin
 */
public class RuleViolationException extends RegistryException {

    private static final long serialVersionUID = 8437151164241883773L;

    @Getter
    private final RuleType ruleType;

    @Getter
    private final Optional<String> ruleConfiguration;

    @Getter
    private final Set<RuleViolation> violations;


    public RuleViolationException(RuleType ruleType, String ruleConfiguration, List<RuleViolation> violations) {
        this(ruleType.value() + " rule violated.", ruleType, ruleConfiguration, new HashSet<>(violations));
    }


    public RuleViolationException(String message, RuleType ruleType, String ruleConfiguration, Set<RuleViolation> violations) {
        super(message);
        this.ruleType = ruleType;
        this.ruleConfiguration = ofNullable(ruleConfiguration);
        this.violations = violations;
    }


    @Override
    public String getMessage() {
        return super.getMessage() + violations.stream()
                .map(rv -> rv.getDescription() + (rv.getContext() != null && !rv.getContext().isBlank() ? " at " + rv.getContext() : ""))
                .reduce((left, right) -> left + ", " + right)
                .map(s -> " Causes: " + s).orElse("");
    }
}
