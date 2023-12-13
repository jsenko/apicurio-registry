package io.apicurio.registry.utils.impexp;

import io.apicurio.registry.impexp.ImpExpRegistryStorage;
import io.apicurio.registry.impexp.ImportSink;
import io.apicurio.registry.storage.RegistryStorageContentUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipInputStream;

public class ZipImporter {


    private final RegistryStorageContentUtils utils;
    private final ImpExpRegistryStorage storage;
    private final boolean preserveGlobalId;
    private final boolean preserveContentId;


    public ZipImporter(RegistryStorageContentUtils utils, ImpExpRegistryStorage storage, boolean preserveGlobalId, boolean preserveContentId) {
        this.utils = utils;
        this.storage = storage;
        this.preserveGlobalId = preserveGlobalId;
        this.preserveContentId = preserveContentId;
    }


    public void importData(InputStream in) {
        ImportSink sink = null;
        try {
            var zip = new ZipInputStream(in, StandardCharsets.UTF_8);
            var reader = new ZipEntityReader(zip);
            // Migration goes here via nested sinks
            sink = new PreparationImportSink(utils, storage, preserveGlobalId, preserveContentId);
            ImportSink finalSink = sink;
            storage.withTransaction(() -> {
                while (!reader.done()) {
                    var entity = reader.readEntity();
                    if (entity != null) {
                        finalSink.importEntity(entity);
                    }
                }
                return null;
            });
        } finally {
            if (sink != null) {
                sink.postImport();
            }
        }
    }
}
