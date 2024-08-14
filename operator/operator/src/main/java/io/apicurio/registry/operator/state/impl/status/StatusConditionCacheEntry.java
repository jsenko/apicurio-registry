package io.apicurio.registry.operator.state.impl.status;

import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class StatusConditionCacheEntry {

    @EqualsAndHashCode.Include
    private StatusConditionCacheEntryKey key;

    // Time at which the condition was created (for the given key)
    private Instant createdTimestamp;

    // Time at which the last change to reason or message happened (without changing the type)
    private Instant updatedTimestamp;

    // Time at which the operator last attempted to add the condition (with or without updates)
    private Instant refreshedTimestamp;

    // What is the next time that reconciliation will be rescheduled for this condition
    private Instant nextRescheduleTimestamp;

    private StatusConditionStatus status;

    // Message is intended to be presented to users in detailed status explanations, such as kubectl describe
    // output.
    private String message;
}
