package io.apicurio.registry.utils.impexp;

import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.impexp.Entity;
import io.apicurio.registry.impexp.ImpExpRegistryStorage;
import io.apicurio.registry.impexp.ImportSink;
import io.apicurio.registry.impexp.v2.*;
import io.apicurio.registry.model.GAV;
import io.apicurio.registry.storage.RegistryStorageContentUtils;
import io.apicurio.registry.storage.SqlUtil;
import io.apicurio.registry.storage.error.RegistryStorageException;
import io.apicurio.registry.storage.error.VersionAlreadyExistsException;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class PreparationImportSink implements ImportSink {

    private static final Logger log = LoggerFactory.getLogger(PreparationImportSink.class);

    private final RegistryStorageContentUtils utils;

    private final ImpExpRegistryStorage storage;

    private final boolean preserveGlobalId;

    private final boolean preserveContentId;

    // To handle the case where we are trying to import a version before its content has been imported
    private final List<ArtifactVersionEntity> waitingForContent = new ArrayList<>();

    // To handle the case where we are trying to import a comment before its version has been imported
    private final List<CommentEntity> waitingForVersion = new ArrayList<>();

    // ID remapping
    private final Map<Long, Long> globalIdMapping = new HashMap<>();
    private final Map<Long, Long> contentIdMapping = new HashMap<>();


    private final Set<GAV> doneGAV = new HashSet<>();
    private final Map<GAV, List<ArtifactBranchEntity>> branchWaitingForGAV = new HashMap<>();


    public PreparationImportSink(RegistryStorageContentUtils utils, ImpExpRegistryStorage storage, boolean preserveGlobalId, boolean preserveContentId) {
        this.utils = utils;
        this.storage = storage;
        this.preserveGlobalId = preserveGlobalId;
        this.preserveContentId = preserveContentId;
    }


    /**
     * WARNING: Must be executed within a transaction!
     */
    @Override
    public void importEntity(Entity entity) {
        switch (entity.getEntityType()) {
            case ARTIFACT_RULE_V2:
                importArtifactRule((ArtifactRuleEntity) entity);
                break;
            case ARTIFACT_VERSION_V2:
                importArtifactVersion((ArtifactVersionEntity) entity);
                break;
            case ARTIFACT_BRANCH_V2:
                importArtifactBranch((ArtifactBranchEntity) entity);
                break;
            case CONTENT_V2:
                importContent((ContentEntity) entity);
                break;
            case GLOBAL_RULE_V2:
                importGlobalRule((GlobalRuleEntity) entity);
                break;
            case GROUP_V2:
                importGroup((GroupEntity) entity);
                break;
            case COMMENT_V2:
                importComment((CommentEntity) entity);
                break;
            case MANIFEST_V2:
                ManifestEntity manifest = (ManifestEntity) entity;
                log.info("---------- Import Info ----------");
                log.info("System Name:    {}", manifest.systemName);
                log.info("System Version: {}", manifest.systemVersion);
                log.info("Data exported on {} by user {}", manifest.exportedOn, manifest.exportedBy);
                log.info("---------- ----------- ----------");
                // Ignore the manifest for now.
                break;
            default:
                throw new RegistryStorageException("Unhandled entity type during import: " + entity.getEntityType());
        }
    }


    @Override
    public void postImport() {
        storage.postImport();
    }

    private void importArtifactRule(ArtifactRuleEntity entity) {
        try {
            storage.importEntity(entity);
            log.debug("Artifact rule imported successfully: {}", entity);
        } catch (Exception ex) {
            log.warn("Failed to import artifact rule {}: {}", entity, ex.getMessage());
        }
    }


    private void importArtifactVersion(ArtifactVersionEntity entity) {
        try {
            // Content needs to be imported before artifact version
            if (!contentIdMapping.containsKey(entity.contentId)) {
                // Add to the queue waiting for content imported
                waitingForContent.add(entity);
                return;
            }

            entity.contentId = contentIdMapping.get(entity.contentId);

            var oldGlobalId = entity.globalId;
            if (!preserveGlobalId) {
                entity.globalId = storage.nextGlobalId();
            }


            storage.importEntity(entity);
            log.debug("Artifact version imported successfully: {}", entity);
            var gav = new GAV(entity.groupId, entity.artifactId, entity.version);
            doneGAV.add(gav);
            globalIdMapping.put(oldGlobalId, entity.globalId);

            // Import branches waiting for this version
            branchWaitingForGAV.computeIfAbsent(gav, _ignored -> List.of()).forEach(this::importArtifactBranch);
            branchWaitingForGAV.remove(gav);

            // Import comments that were waiting for this version
            var commentsToImport = waitingForVersion.stream()
                    .filter(comment -> comment.globalId == oldGlobalId)
                    .collect(Collectors.toList());
            for (CommentEntity commentEntity : commentsToImport) {
                importComment(commentEntity);
            }
            waitingForVersion.removeAll(commentsToImport);

        } catch (VersionAlreadyExistsException ex) {
            if (ex.getGlobalId() != null) {
                log.warn("Duplicate globalId {} detected, skipping import of artifact version: {}", ex.getGlobalId(), entity);
            } else {
                log.warn("Failed to import artifact version {}: {}", entity, ex.getMessage());
            }
        } catch (Exception ex) {
            log.warn("Failed to import artifact version {}: {}", entity, ex.getMessage());
        }
    }


    private void importContent(ContentEntity entity) {
        try {
            List<io.apicurio.registry.model.ArtifactReferenceDto> references = SqlUtil.deserializeReferences(entity.serializedReferences);

            // We do not need canonicalHash if we have artifactType
            if (entity.canonicalHash == null && entity.artifactType != null) {
                ContentHandle canonicalContent = utils.canonicalizeContent(
                        entity.artifactType, entity.content,
                        storage.resolveReferences(references));
                entity.canonicalHash = DigestUtils.sha256Hex(canonicalContent.bytes());
            }


            var oldContentId = entity.contentId;
            if (!preserveContentId) {
                entity.contentId = storage.nextContentId();
            }

            storage.importEntity(entity);
            log.debug("Content imported successfully: {}", entity);

            contentIdMapping.put(oldContentId, entity.contentId);

            // Import artifact versions that were waiting for this content
            var artifactsToImport = waitingForContent.stream()
                    .filter(artifactVersion -> artifactVersion.contentId == oldContentId)
                    .collect(Collectors.toList());

            for (ArtifactVersionEntity artifactVersionEntity : artifactsToImport) {
                artifactVersionEntity.contentId = entity.contentId;
                importArtifactVersion(artifactVersionEntity);
            }
            waitingForContent.removeAll(artifactsToImport);

        } catch (Exception ex) {
            log.warn("Failed to import content {}: {}", entity, ex.getMessage());
        }
    }


    private void importGlobalRule(GlobalRuleEntity entity) {
        try {
            storage.importEntity(entity);
            log.debug("Global rule imported successfully: {}", entity);
        } catch (Exception ex) {
            log.warn("Failed to import global rule {}: {}", entity, ex.getMessage());
        }
    }


    private void importGroup(GroupEntity entity) {
        try {
            storage.importEntity(entity);
            log.debug("Group imported successfully: {}", entity);
        } catch (Exception ex) {
            log.warn("Failed to import group {}: {}", entity, ex.getMessage());
        }
    }


    private void importComment(CommentEntity entity) {
        try {
            if (!globalIdMapping.containsKey(entity.globalId)) {
                // The version hasn't been imported yet.  Need to wait for it.
                waitingForVersion.add(entity);
                return;
            }
            entity.globalId = globalIdMapping.get(entity.globalId);

            storage.importEntity(entity);
            log.debug("Comment imported successfully: {}", entity);
        } catch (Exception ex) {
            log.warn("Failed to import comment {}: {}", entity, ex.getMessage());
        }
    }


    private void importArtifactBranch(ArtifactBranchEntity entity) {
        try {
            if (doneGAV.contains(entity.toGAV())) {
                storage.importEntity(entity);
            } else {
                branchWaitingForGAV.computeIfAbsent(entity.toGAV(), _ignored -> new ArrayList<>()).add(entity);
            }
        } catch (Exception ex) {
            log.warn("Failed to import artifact branch {}: {}", entity, ex.getMessage());
        }
    }
}
