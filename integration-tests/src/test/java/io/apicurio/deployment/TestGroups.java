package io.apicurio.deployment;

import java.util.Optional;

/**
 * JUnit 5 test group tag constants and utilities.
 */
public final class TestGroups {

    private TestGroups() {
    }

    public static final String SMOKE = "smoke";
    public static final String SERDES = "serdes";
    public static final String ACCEPTANCE = "acceptance";
    public static final String MIGRATION = "migration";
    public static final String AUTH = "auth";
    public static final String KAFKA_SQL_SNAPSHOTTING = "kafkasql-snapshotting";
    public static final String DEBEZIUM = "debezium";
    public static final String DEBEZIUM_MYSQL = "debezium-mysql";
    public static final String DEBEZIUM_SNAPSHOT = "debezium-snapshot";
    public static final String DEBEZIUM_MYSQL_SNAPSHOT = "debezium-mysql-snapshot";
    public static final String KUBERNETES_OPS = "kubernetesopsit";
    public static final String ICEBERG = "iceberg";
    public static final String SEARCH = "search";

    private static final String ACTIVE_GROUPS =
            Optional.ofNullable(System.getProperty("groups")).orElse("");

    /**
     * Check whether any of the given tags match the active test groups using exact matching.
     */
    public static boolean isAnyGroupActive(final String... tags) {
        for (String g : ACTIVE_GROUPS.split("[|,]")) {
            String group = g.trim();
            for (String tag : tags) {
                if (group.equals(tag)) {
                    return true;
                }
            }
        }
        return false;
    }
}
