package io.apicurio.registry.impexp;


public interface ExportSource {


    /**
     * Method {@link ExportSource#postExport()} MUST be called after the export process is done.
     */
    void export(ImportSink sink);


    default void postExport() {
        // NOOP
    }
}
