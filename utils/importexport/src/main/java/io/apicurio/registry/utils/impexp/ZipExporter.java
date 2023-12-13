package io.apicurio.registry.utils.impexp;

import io.apicurio.registry.impexp.ExportSource;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipOutputStream;

public class ZipExporter {


    private final ExportSource source;


    public ZipExporter(ExportSource source) {
        this.source = source;
    }


    public void export(OutputStream outputStream) {
        try {
            ZipOutputStream zip = new ZipOutputStream(outputStream, StandardCharsets.UTF_8);
            var writer = new ZipEntityWriter(zip);
            source.export(writer);
        } finally {
            source.postExport();
        }
    }
}
