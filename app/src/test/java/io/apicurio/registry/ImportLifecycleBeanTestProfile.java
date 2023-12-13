package io.apicurio.registry;

import io.apicurio.registry.utils.migration.Migrate;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.lang.System.err;
import static java.util.Objects.requireNonNull;

public class ImportLifecycleBeanTestProfile implements QuarkusTestProfile {


    @Override
    public Map<String, String> getConfigOverrides() {
        try {
            var inputFile = Path.of(requireNonNull(getClass().getResource("rest/v2/export.zip")).toExternalForm().replaceFirst("file:", ""));
            err.println("Input file: " + inputFile);
            var outputFile = Files.createTempDirectory(getClass().getCanonicalName() + "-").resolve("export-v2.zip");
            err.println("Output file: " + outputFile);
            Migrate.migrate(inputFile, outputFile);
            return Map.of("registry.import.url", "file:" + outputFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
