package io.apicurio.registry.impexp;


public interface ImportSink {


    /**
     * Method {@link ImportSink#postImport()} MUST be called after the import process is done.
     */
    void importEntity(Entity entity);


    default void postImport() {
        // NOOP
    }
}
