package io.apicurio.registry.operator.state.impl.status;

import io.apicurio.registry.operator.context.CRContext;
import io.apicurio.registry.operator.state.State;
import io.fabric8.kubernetes.api.model.Condition;
import io.fabric8.kubernetes.api.model.ConditionBuilder;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.tuples.Tuple3;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.apicurio.registry.operator.utils.LogUtils.contextPrefix;
import static java.time.Instant.now;
import static java.util.Comparator.comparing;

/**
 * Handle collecting and reporting of status conditions.
 */
public class StatusConditionCache implements State {

    private static final Logger log = LoggerFactory.getLogger(StatusConditionCache.class);

    private final Map<StatusConditionCacheEntryKey, StatusConditionCacheEntry> cache = new HashMap<>();

    @Getter
    private boolean changed = false;

    private Instant lastReportTime = Instant.MIN;

    /**
     * Report a status condition. In general, you don't have to handle removing reported conditions, that is
     * handled automatically depending on the type. It should be enough to just report a condition when it
     * occurs.
     */
    public void updateStatusCondition(CRContext crContext, StatusConditionEntry entry) {
        var changed_ = false;
        var now = now();

        // spotless:off
        var statusEntryKey = StatusConditionCacheEntryKey.builder()
                .type(entry.getType())
                .reason(entry.getReason())
                .build();
        // spotless:on

        // spotless:off
        var newStatusEntry = StatusConditionCacheEntry.builder()
                .key(statusEntryKey)
                .status(entry.getStatus())
                .message(entry.getMessage())
                .createdTimestamp(now)
                .updatedTimestamp(now)
                .refreshedTimestamp(now)
                .build();
        // spotless:on

        var existingStatusEntry = cache.get(statusEntryKey);

        if (existingStatusEntry != null) {

            if (!Objects.equals(newStatusEntry.getMessage(), existingStatusEntry.getMessage())) {
                newStatusEntry.setMessage(existingStatusEntry.getMessage());
                changed_ = true;
            }

            if (!Objects.equals(newStatusEntry.getStatus(), existingStatusEntry.getStatus())) {

                changed_ = !Objects.equals(newStatusEntry.getStatus(),
                        newStatusEntry.getKey().getType().getDefaultStatus())
                        || existingStatusEntry.getUpdatedTimestamp().isAfter(lastReportTime);
            }

            if (changed_) {
                newStatusEntry.setCreatedTimestamp(existingStatusEntry.getCreatedTimestamp());
                newStatusEntry.setUpdatedTimestamp(existingStatusEntry.getUpdatedTimestamp());
            }

        } else {
            changed_ = true;
        }

        if (changed_) {
            cache.put(statusEntryKey, newStatusEntry);
            changed = true;
            log.warn("{}Status condition has been updated: {}", contextPrefix(crContext), newStatusEntry);
        } else {
            existingStatusEntry.setRefreshedTimestamp(now);
            log.debug("{}Status condition has been refreshed: {}", contextPrefix(crContext), newStatusEntry);
        }
    }

    /**
     * Iterate over entries in the cache, and figure out: - Which conditions should be removed - Number of
     * seconds after which we should reschedule reconciliation to update status again
     */
    private Long refresh(CRContext crContext, boolean resetRescheduleTimes) {
        var now = now();

        // Figure out which condition entries can we remove
        var remove = new HashSet<StatusConditionCacheEntryKey>();

        for (StatusConditionCacheEntry entry : cache.values()) {
            var expiration = entry.getKey().getType().getExpiration();

            if (expiration != null) {
                if (entry.getRefreshedTimestamp().plus(expiration).isAfter(now)) {
                    // expired because of expiration configuration
                    remove.add(entry.getKey());
                    log.debug("{}Status condition will be removed because it expired: {}",
                            contextPrefix(crContext), entry.getKey());
                }
            } else {
                if (entry.getRefreshedTimestamp().isBefore(lastReportTime)) {
                    // expired because it was not refreshed this reconciliation round
                    remove.add(entry.getKey());
                    log.debug(
                            "{}Status condition will be removed because it was not refreshed during current reconciliation: {}",
                            contextPrefix(crContext), entry.getKey());
                }
            }
            if (entry.getKey().getType().isHideWhenDefaultStatus()
                    && entry.getKey().getType().getDefaultStatus().equals(entry.getStatus())) {
                // remove condition with default status
                remove.add(entry.getKey());
                log.debug("{}Status condition will be removed because it has default status: {}",
                        contextPrefix(crContext), entry.getKey());
            }
        }
        // remove
        for (StatusConditionCacheEntryKey entryKey : remove) {
            cache.remove(entryKey);
            changed = true;
            log.info("{}Status condition has been removed: {}", contextPrefix(crContext), entryKey);
        }
        Long rescheduleSec = Long.MAX_VALUE;
        for (StatusConditionCacheEntry entry : cache.values()) {
            if (entry.getKey().getType().getReschedule() != null) {
                if (entry.getNextRescheduleTimestamp() == null) {
                    // We need to request a rescheduling for the first time
                    rescheduleSec = Math.min(rescheduleSec,
                            entry.getKey().getType().getReschedule().getSeconds());
                } else if (now.isAfter(entry.getNextRescheduleTimestamp())) {
                    // We need to request a rescheduling again
                    var diffSecs = Duration.between(entry.getNextRescheduleTimestamp(), now).getSeconds();
                    var nextSecs = entry.getKey().getType().getReschedule().getSeconds() - diffSecs;
                    rescheduleSec = Math.min(rescheduleSec, Math.max(0L, nextSecs));
                }
            }
        }
        // TODO Use a common reschedule timestamp
        if (rescheduleSec == Long.MAX_VALUE) {
            rescheduleSec = null;
        } else {
            if (resetRescheduleTimes) {
                for (StatusConditionCacheEntry entry : cache.values()) {
                    entry.setNextRescheduleTimestamp(now.plusSeconds(rescheduleSec));
                }
            }
        }
        return rescheduleSec;
    }

    /**
     * @return a list of conditions to show in the status (can be empty but not null), and reschedule timeout
     *         (seconds) (can be null).
     */
    public Tuple2<List<Condition>, Long> getConditions(CRContext crContext, boolean resetRescheduleTimes) {

        var rescheduleSec = refresh(crContext, resetRescheduleTimes);

        var conditions = new ArrayList<Condition>();

        // spotless:off
        cache.values().stream()
                .sorted(comparing(StatusConditionCacheEntry::getKey, StatusConditionCacheEntryKey.COMPARATOR))
                .forEach(entry -> {
                    conditions.add(new ConditionBuilder()
                            .withType(entry.getKey().getType().getValue())
                            .withStatus(entry.getStatus().getValue())
                            .withMessage(entry.getMessage())
                            .withReason(entry.getKey().getReason())
                            .withLastTransitionTime(entry.getUpdatedTimestamp().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT))
                            .build());
                });
        // spotless:on

        lastReportTime = now();
        return Tuple3.of(conditions, rescheduleSec);
    }

    @Override
    public void afterReconciliation() {
        changed = false;
    }
}
