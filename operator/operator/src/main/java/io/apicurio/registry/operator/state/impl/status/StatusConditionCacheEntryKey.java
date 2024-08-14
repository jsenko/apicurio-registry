package io.apicurio.registry.operator.state.impl.status;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Comparator;

import static java.util.Comparator.comparing;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class StatusConditionCacheEntryKey {

    @EqualsAndHashCode.Include
    private StatusConditionType type;

    // Reason is intended to be a one-word, CamelCase representation of the category of cause of the current
    // status
    // Use exception class name
    private String reason;

    public static final Comparator<StatusConditionCacheEntryKey> COMPARATOR = comparing(
            (StatusConditionCacheEntryKey key) -> key.type).thenComparing(key -> key.reason);
}
