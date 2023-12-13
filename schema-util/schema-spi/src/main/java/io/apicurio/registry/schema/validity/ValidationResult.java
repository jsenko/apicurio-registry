package io.apicurio.registry.schema.validity;

import io.apicurio.registry.schema.compat.RuleViolation;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
@Getter
@ToString
public class ValidationResult {

    public static ValidationResult SUCCESS_EMPTY = new ValidationResult(List.of());


    @Singular
    private List<RuleViolation> violations;


    public boolean isValid() {
        return violations.isEmpty();
    }


    public static ValidationResult of(RuleViolation violation) {
        return new ValidationResult(List.of(violation));
    }


    public ValidationResult merge(ValidationResult other) {
        return new ValidationResult(Stream.concat(violations.stream(), other.violations.stream())
                .collect(Collectors.toList()));
    }
}
