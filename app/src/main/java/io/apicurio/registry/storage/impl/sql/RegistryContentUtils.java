package io.apicurio.registry.storage.impl.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.registry.content.ContentHandle;
import io.apicurio.registry.content.TypedContent;
import io.apicurio.registry.content.refs.JsonPointerExternalReference;
import io.apicurio.registry.storage.dto.ArtifactReferenceDto;
import io.apicurio.registry.storage.dto.ContentWrapperDto;
import io.apicurio.registry.types.RegistryException;
import io.apicurio.registry.types.provider.ArtifactTypeUtilProvider;
import io.apicurio.registry.types.provider.ArtifactTypeUtilProviderFactory;
import io.apicurio.registry.types.provider.DefaultArtifactTypeUtilProviderImpl;
import io.apicurio.registry.utils.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RegistryContentUtils {

    private static final Logger log = LoggerFactory.getLogger(RegistryContentUtils.class);

    private static final String NULL_GROUP_ID = "__$GROUPID$__";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final ArtifactTypeUtilProviderFactory ARTIFACT_TYPE_UTIL = new DefaultArtifactTypeUtilProviderImpl();

    private RegistryContentUtils() {
    }

    /**
     * Recursively resolve the references.
     */
    public static Map<String, TypedContent> recursivelyResolveReferences(
            List<ArtifactReferenceDto> references, Function<ArtifactReferenceDto, ContentWrapperDto> loader) {
        if (references == null || references.isEmpty()) {
            return Map.of();
        } else {
            Map<String, TypedContent> result = new LinkedHashMap<>();
            resolveReferences(result, references, loader);
            return result;
        }
    }

    /**
     * Recursively resolve the references. Instead of using the reference name as the key, it uses the full
     * coordinates of the artifact version. Re-writes each schema node content to use the full coordinates of
     * the artifact version instead of just using the original reference name.
     * 
     * @return the main content rewritten to use the full coordinates of the artifact version and the full
     *         tree of dependencies, also rewritten to use coordinates instead of the reference name.
     */
    public static RewrittenContentHolder recursivelyResolveReferencesWithContext(TypedContent mainContent,
            String mainContentType, List<ArtifactReferenceDto> references,
            Function<ArtifactReferenceDto, ContentWrapperDto> loader) {
        if (references == null || references.isEmpty()) {
            return new RewrittenContentHolder(mainContent, Collections.emptyMap());
        } else {
            Map<String, TypedContent> resolvedReferences = new LinkedHashMap<>();
            // First we resolve all the references tree, re-writing the nested contents to use the artifact
            // version coordinates instead of the reference name.
            return resolveReferencesWithContext(mainContent, mainContentType, resolvedReferences, references,
                    loader, new HashMap<>());
        }
    }

    /**
     * Recursively resolve the references. Instead of using the reference name as the key, it uses the full
     * coordinates of the artifact version. Re-writes each schema node content to use the full coordinates of
     * the artifact version instead of just using the original reference name. This allows to dereference json
     * schema artifacts where there might be duplicate file names in a single hierarchy.
     */
    private static RewrittenContentHolder resolveReferencesWithContext(TypedContent mainContent,
            String schemaType, Map<String, TypedContent> partialRecursivelyResolvedReferences,
            List<ArtifactReferenceDto> references, Function<ArtifactReferenceDto, ContentWrapperDto> loader,
            Map<String, String> referencesRewrites) {
        if (references != null && !references.isEmpty()) {
            for (ArtifactReferenceDto reference : references) {
                if (reference.getArtifactId() == null || reference.getName() == null
                        || reference.getVersion() == null) {
                    throw new IllegalStateException("Invalid reference: " + reference);
                } else {
                    String refName = reference.getName();
                    String referenceCoordinates = concatArtifactVersionCoordinatesWithRefName(
                            reference.getGroupId(), reference.getArtifactId(), reference.getVersion(),
                            refName);

                    JsonPointerExternalReference refPointer = new JsonPointerExternalReference(refName);
                    JsonPointerExternalReference coordinatePointer = new JsonPointerExternalReference(
                            referenceCoordinates, refPointer.getComponent());

                    String newRefName = coordinatePointer.toString();

                    if (!partialRecursivelyResolvedReferences.containsKey(newRefName)) {
                        try {
                            var nested = loader.apply(reference);
                            if (nested != null) {
                                ArtifactTypeUtilProvider typeUtilProvider = ARTIFACT_TYPE_UTIL
                                        .getArtifactTypeProvider(nested.getArtifactType());
                                RewrittenContentHolder rewrittenContentHolder = resolveReferencesWithContext(
                                        TypedContent.create(nested.getContent(), nested.getArtifactType()),
                                        nested.getArtifactType(), partialRecursivelyResolvedReferences,
                                        nested.getReferences(), loader, referencesRewrites);
                                referencesRewrites.put(refName, referenceCoordinates);
                                TypedContent rewrittenContent = typeUtilProvider.getContentDereferencer()
                                        .rewriteReferences(rewrittenContentHolder.getRewrittenContent(),
                                                referencesRewrites);
                                partialRecursivelyResolvedReferences.put(newRefName, rewrittenContent);
                            }
                        } catch (Exception ex) {
                            log.error("Could not resolve reference " + reference + ".", ex);
                        }
                    }
                }
            }
        }
        ArtifactTypeUtilProvider typeUtilProvider = ARTIFACT_TYPE_UTIL.getArtifactTypeProvider(schemaType);
        TypedContent rewrittenContent = typeUtilProvider.getContentDereferencer()
                .rewriteReferences(mainContent, referencesRewrites);
        return new RewrittenContentHolder(rewrittenContent, partialRecursivelyResolvedReferences);
    }

    private static void resolveReferences(Map<String, TypedContent> partialRecursivelyResolvedReferences,
            List<ArtifactReferenceDto> references, Function<ArtifactReferenceDto, ContentWrapperDto> loader) {
        if (references != null && !references.isEmpty()) {
            for (ArtifactReferenceDto reference : references) {
                if (reference.getArtifactId() == null || reference.getName() == null
                        || reference.getVersion() == null) {
                    throw new IllegalStateException("Invalid reference: " + reference);
                } else {
                    if (!partialRecursivelyResolvedReferences.containsKey(reference.getName())) {
                        try {
                            var nested = loader.apply(reference);
                            if (nested != null) {
                                resolveReferences(partialRecursivelyResolvedReferences,
                                        nested.getReferences(), loader);
                                partialRecursivelyResolvedReferences.put(reference.getName(),
                                        TypedContent.create(nested.getContent(), nested.getArtifactType()));
                            }
                        } catch (Exception ex) {
                            log.error("Could not resolve reference " + reference + ".", ex);
                        }
                    }
                }
            }
        }
    }

    /**
     * Canonicalize the given content.
     * <p>
     * WARNING: Fails silently.
     */
    private static TypedContent canonicalizeContent(String artifactType, TypedContent content,
            Map<String, TypedContent> recursivelyResolvedReferences) {
        try {
            return ARTIFACT_TYPE_UTIL.getArtifactTypeProvider(artifactType).getContentCanonicalizer()
                    .canonicalize(content, recursivelyResolvedReferences);
        } catch (Exception ex) {
            // TODO: We should consider explicitly failing when a content could not be canonicalized.
            // throw new RegistryException("Failed to canonicalize content.", ex);
            log.debug("Failed to canonicalize content: {}", content.getContent());
            return content;
        }
    }

    /**
     * Canonicalize the given content.
     *
     * @throws RegistryException in the case of an error.
     */
    public static TypedContent canonicalizeContent(String artifactType, ContentWrapperDto data,
            Function<ArtifactReferenceDto, ContentWrapperDto> loader) {
        try {
            return canonicalizeContent(artifactType,
                    TypedContent.create(data.getContent(), data.getArtifactType()),
                    recursivelyResolveReferences(data.getReferences(), loader));
        } catch (Exception ex) {
            throw new RegistryException("Failed to canonicalize content.", ex);
        }
    }

    /**
     * @param loader can be null *if and only if* references are empty.
     */
    public static String canonicalContentHash(String artifactType, ContentWrapperDto data,
            Function<ArtifactReferenceDto, ContentWrapperDto> loader) {
        try {
            if (notEmpty(data.getReferences())) {
                String serializedReferences = serializeReferences(data.getReferences());
                TypedContent canonicalContent = canonicalizeContent(artifactType, data, loader);
                return DigestUtils.sha256Hex(concatContentAndReferences(canonicalContent.getContent().bytes(),
                        serializedReferences));
            } else {
                TypedContent canonicalContent = canonicalizeContent(artifactType,
                        TypedContent.create(data.getContent(), data.getArtifactType()), Map.of());
                return DigestUtils.sha256Hex(canonicalContent.getContent().bytes());
            }
        } catch (IOException ex) {
            throw new RegistryException("Failed to compute canonical content hash.", ex);
        }
    }

    /**
     * data.references may be null
     */
    public static String contentHash(ContentWrapperDto data) {
        try {
            if (notEmpty(data.getReferences())) {
                String serializedReferences = serializeReferences(data.getReferences());
                return DigestUtils.sha256Hex(
                        concatContentAndReferences(data.getContent().bytes(), serializedReferences));
            } else {
                return data.getContent().getSha256Hash();
            }
        } catch (IOException ex) {
            throw new RegistryException("Failed to compute content hash.", ex);
        }
    }

    private static byte[] concatContentAndReferences(byte[] contentBytes, String serializedReferences)
            throws IOException {
        if (serializedReferences != null && !serializedReferences.isEmpty()) {
            var serializedReferencesBytes = ContentHandle.create(serializedReferences).bytes();
            var bytes = ByteBuffer.allocate(contentBytes.length + serializedReferencesBytes.length);
            bytes.put(contentBytes);
            bytes.put(serializedReferencesBytes);
            return bytes.array();
        } else {
            throw new IllegalArgumentException("serializedReferences is null or empty");
        }
    }

    /**
     * Serializes the given collection of labels to a string for artifactStore in the DB.
     *
     * @param labels
     */
    public static String serializeLabels(Map<String, String> labels) {
        try {
            if (labels == null) {
                return null;
            }
            if (labels.isEmpty()) {
                return null;
            }
            return MAPPER.writeValueAsString(labels);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialize the labels from their string form to a <code>List&lt;String&gt;</code> form.
     *
     * @param labelsStr
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> deserializeLabels(String labelsStr) {
        try {
            if (StringUtil.isEmpty(labelsStr)) {
                return null;
            }
            return MAPPER.readValue(labelsStr, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Serializes the given collection of references to a string for artifactStore in the DB.
     *
     * @param references
     */
    public static String serializeReferences(List<ArtifactReferenceDto> references) {
        try {
            if (references == null || references.isEmpty()) {
                return null;
            }
            return MAPPER.writeValueAsString(references);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialize the references from their string form to a List<ArtifactReferenceDto> form.
     *
     * @param references
     */
    public static List<ArtifactReferenceDto> deserializeReferences(String references) {
        try {
            if (StringUtil.isEmpty(references)) {
                return Collections.emptyList();
            }
            return MAPPER.readValue(references, new TypeReference<List<ArtifactReferenceDto>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String normalizeGroupId(String groupId) {
        if (groupId == null || "default".equals(groupId)) {
            return NULL_GROUP_ID;
        }
        return groupId;
    }

    public static String denormalizeGroupId(String groupId) {
        if (NULL_GROUP_ID.equals(groupId)) {
            return null;
        }
        return groupId;
    }

    public static boolean notEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static String concatArtifactVersionCoordinatesWithRefName(String groupId, String artifactId,
            String version, String referenceName) {
        return groupId + ":" + artifactId + ":" + version + ":" + referenceName;
    }

    public static class RewrittenContentHolder {
        final TypedContent rewrittenContent;
        final Map<String, TypedContent> resolvedReferences;

        public RewrittenContentHolder(TypedContent rewrittenContent,
                Map<String, TypedContent> resolvedReferences) {
            this.rewrittenContent = rewrittenContent;
            this.resolvedReferences = resolvedReferences;
        }

        public TypedContent getRewrittenContent() {
            return rewrittenContent;
        }

        public Map<String, TypedContent> getResolvedReferences() {
            return resolvedReferences;
        }
    }
}
