package io.apicurio.registry.utils.migration;

import io.apicurio.registry.utils.impexp.ZipExporter;
import io.apicurio.registry.utils.migration.impexp.EntityInputStream;
import io.apicurio.registry.utils.migration.impexp.MigratingRegistryStorageImpl;
import io.apicurio.registry.utils.migration.impexp.entity.Entity;
import io.apicurio.registry.utils.migration.impexp.entity.EntityReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

import static java.lang.System.err;

public class Migrate {


    private Migrate() {
    }


    public static void migrate(Path inputFile, Path outputFile) {
        try {
            var storage = new MigratingRegistryStorageImpl();

            try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile.toFile()))) {

                var zip = new ZipInputStream(in, StandardCharsets.UTF_8);

                final EntityReader reader = new EntityReader(zip);

                try (EntityInputStream stream = new EntityInputStream() {

                    @Override
                    public Entity nextEntity() {
                        try {
                            return reader.readEntity();
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    @Override
                    public void close() throws IOException {
                        zip.close();
                    }
                }) {
                    storage.importData(stream, true, true);
                }
            }

            try (var out = new FileOutputStream(outputFile.toFile())) {
                var zip = new ZipExporter(storage);
                zip.export(out);
            }

            err.println("Migration successful.");

        } catch (Exception ex) {
            err.println("Migration failed: " + ex.getMessage());
            ex.printStackTrace(err);
        }
    }
}
