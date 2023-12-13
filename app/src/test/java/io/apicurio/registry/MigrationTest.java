/*
 * Copyright 2022 Red Hat
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

package io.apicurio.registry;

import io.apicurio.registry.utils.migration.Migrate;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static java.lang.System.err;
import static java.util.Objects.requireNonNull;

@QuarkusTest
public class MigrationTest extends AbstractResourceTestBase {


    @Test
    public void migrateData() throws Exception {


        var originalData = Path.of(requireNonNull(getClass().getResource("rest/v2/destination_original_data.zip")).toExternalForm().replaceFirst("file:", ""));
        err.println("Original file: " + originalData);
        var originalDataV2 = Files.createTempDirectory(getClass().getCanonicalName() + "-").resolve("destination_original_data-v2.zip");
        err.println("Original V2 file: " + originalDataV2);
        Migrate.migrate(originalData, originalDataV2);

        var migratedData = Path.of(requireNonNull(getClass().getResource("rest/v2/migration_test_data_dump.zip")).toExternalForm().replaceFirst("file:", ""));
        err.println("Migrated file: " + migratedData);
        var migratedDataV2 = Files.createTempDirectory(getClass().getCanonicalName() + "-").resolve("migration_test_data_dump-v2.zip");
        err.println("Migrated V2 file: " + migratedDataV2);
        Migrate.migrate(migratedData, migratedDataV2);

        try (
                InputStream originalDataStream = new FileInputStream(originalDataV2.toFile());
                InputStream migratedDataStream = new FileInputStream(migratedDataV2.toFile())
        ) {

            clientV2.admin().importEscaped().post(originalDataStream, config -> {
                // TODO: this header should be injected by Kiota
                config.headers.add("Content-Type", "application/zip");
            }).get(10, TimeUnit.SECONDS);
            clientV2.admin().importEscaped().post(migratedDataStream, config -> {
                // TODO: this header should be injected by Kiota
                config.headers.add("Content-Type", "application/zip");
                config.headers.add("X-Registry-Preserve-GlobalId", "false");
                config.headers.add("X-Registry-Preserve-ContentId", "false");
            }).get(40, TimeUnit.SECONDS);
        }
    }
}
