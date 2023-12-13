package io.apicurio.registry.schema.compat;

import java.util.Collections;
import java.util.Set;

/**
 * Created by aohana
 * <p>
 * Holds the result for a compatibility check
 * incompatibleDifferences - will contain values in case the schema type has difference type information in case the
 * new schema is not compatible (only JSON schema as of now)
 */
public class CompatibilityExecutionResult {

    private final Set<CompatibilityDifference> incompatibleDifferences;

    public CompatibilityExecutionResult(Set<CompatibilityDifference> incompatibleDifferences) {
        this.incompatibleDifferences = incompatibleDifferences;
    }

    public boolean isCompatible() {
        return incompatibleDifferences == null || incompatibleDifferences.isEmpty();
    }

    public Set<CompatibilityDifference> getIncompatibleDifferences() {
        return incompatibleDifferences;
    }

    public static CompatibilityExecutionResult compatible() {
        return new CompatibilityExecutionResult(Collections.emptySet());
    }

    /**
     * Creates an instance of {@link CompatibilityExecutionResult} that represents "incompatible" results.  This
     * variant takes the set of {@link CompatibilityDifference}s as the basis of the result.  A non-zero number
     * of differences indicates incompatibility.
     */
    public static CompatibilityExecutionResult incompatibleOrEmpty(Set<CompatibilityDifference> incompatibleDifferences) {
        return new CompatibilityExecutionResult(incompatibleDifferences);
    }


}
