package io.apicurio.registry.utils.migration.impexp;

import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.schema.ArtifactTypeUtilProviderFactory;
import io.apicurio.registry.types.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * TODO Refactor
 * TODO Cache calls to referenceResolver
 *
 * @author Jakub Senko <em>m@jsenko.net</em>
 */
public class PartialRegistryStorageContentUtils {

    private static final Logger log = LoggerFactory.getLogger(PartialRegistryStorageContentUtils.class);

    ArtifactTypeUtilProviderFactory factory;

    public PartialRegistryStorageContentUtils(ArtifactTypeUtilProviderFactory factory) {
        this.factory = factory;
    }

    /**
     * Canonicalize the given content.
     *
     * @throws RegistryException in the case of an error.
     */
    public ContentHandle canonicalizeContent(String artifactType, ContentHandle content, Map<String, ContentHandle> resolvedReferences) {
        try {
            return factory.getArtifactTypeProvider(artifactType)
                    .getContentCanonicalizer()
                    .canonicalize(content, resolvedReferences);
        } catch (Exception ex) {
            // TODO: We should consider explicitly failing when a content could not be canonicalized.
            // throw new RegistryException("Failed to canonicalize content.", ex);
            log.debug("Failed to canonicalize content: {}", content.content());
            return content;
        }
    }
}
