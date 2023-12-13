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

package io.apicurio.registry.rules.compatibility;

import io.apicurio.registry.schema.compat.CompatibilityDifference;
import io.apicurio.registry.schema.compat.CompatibilityExecutionResult;
import io.apicurio.registry.schema.compat.RuleViolation;

import java.util.Collections;
import java.util.Objects;

/**
 * @author eric.wittmann@gmail.com
 */
public class SimpleCompatibilityDifference implements CompatibilityDifference {

    private final RuleViolation ruleViolation;

    public SimpleCompatibilityDifference(String description, String context) {
        Objects.requireNonNull(description);
        if (context == null || context.isBlank()) {
            context = "/";
        }
        ruleViolation = new RuleViolation(description, context);
    }

    public SimpleCompatibilityDifference(String description) {
        this(description, null);
    }

    @Override
    public RuleViolation asRuleViolation() {
        return ruleViolation;
    }


    /**
     * Creates an instance of {@link CompatibilityExecutionResult} that represents "incompatible" results.  This
     * variant takes an Exception and converts that into a set of differences.  Ideally this would never be used,
     * but some artifact types do not have the level of granularity to report individual differences.
     */
    public static CompatibilityExecutionResult incompatible(Exception e) {
        CompatibilityDifference diff = new SimpleCompatibilityDifference(e.getMessage());
        return new CompatibilityExecutionResult(Collections.singleton(diff));
    }


    /**
     * Creates an instance of {@link CompatibilityExecutionResult} that represents "incompatible" results.  This
     * variant takes a message.
     */
    public static CompatibilityExecutionResult incompatible(String message) {
        CompatibilityDifference diff = new SimpleCompatibilityDifference(message);
        return new CompatibilityExecutionResult(Collections.singleton(diff));
    }
}
