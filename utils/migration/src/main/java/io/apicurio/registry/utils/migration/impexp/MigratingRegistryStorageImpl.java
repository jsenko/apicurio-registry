package io.apicurio.registry.utils.migration.impexp;

import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.impexp.ExportSource;
import io.apicurio.registry.impexp.ImportSink;
import io.apicurio.registry.impexp.v2.ArtifactBranchEntity;
import io.apicurio.registry.model.*;
import io.apicurio.registry.types.provider.ArtifactTypeUtilProviderFactoryImpl;
import io.apicurio.registry.utils.migration.impexp.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

public class MigratingRegistryStorageImpl implements LegacyRegistryStorage, ExportSource {

    private static final Logger log = LoggerFactory.getLogger(MigratingRegistryStorageImpl.class);

    private final Map<GroupId, GroupEntity> groupEntityMap = new HashMap<>();
    private final Map<String, GlobalRuleEntity> globalRuleMap = new HashMap<>();
    private final Map<Long, ContentEntity> contentIdMap = new HashMap<>();
    private final Map<GAV, ArtifactVersionEntity> artifactVersionMap = new HashMap<>();
    private final Map<GA, List<ArtifactRuleEntity>> artifactRuleMap = new HashMap<>();
    private final Map<Long, List<CommentEntity>> commentMap = new HashMap<>();
    //private ManifestEntity manifest;


    @Override
    public void export(ImportSink handler) {
        log.debug("Calling exportData.");
        // Migrate and export V2 data

        // Migrate content
        for (var entityV1 : contentIdMap.values()) {
            var entityV2 = new io.apicurio.registry.impexp.v2.ContentEntity();
            entityV2.content = ContentHandle.create(entityV1.contentBytes);
            entityV2.artifactType = entityV1.artifactType;
            entityV2.contentId = entityV1.contentId;
            entityV2.canonicalHash = entityV1.canonicalHash;
            entityV2.contentHash = entityV1.contentHash;
            entityV2.serializedReferences = entityV1.serializedReferences;
            handler.importEntity(entityV2);
        }


        for (var groupV1 : groupEntityMap.values()) {
            var groupV2 = new io.apicurio.registry.impexp.v2.GroupEntity();
            groupV2.groupId = groupV1.groupId;
            groupV2.createdBy = groupV1.createdBy;
            groupV2.createdOn = groupV1.createdOn;
            groupV2.modifiedBy = groupV1.modifiedBy;
            groupV2.artifactsType = groupV1.artifactsType; // TODO where is this used?
            groupV2.description = groupV1.description;
            groupV2.properties = groupV1.properties;
            groupV2.modifiedOn = groupV1.modifiedOn;
            handler.importEntity(groupV2);
        }


        var artifactMap = new HashMap<GA, List<io.apicurio.registry.impexp.v2.ArtifactVersionEntity>>();

        // Migrate artifact versions
        for (var entityV1 : artifactVersionMap.values()) {
            var entityV2 = new io.apicurio.registry.impexp.v2.ArtifactVersionEntity();
            entityV2.artifactType = entityV1.artifactType;
            entityV2.version = entityV1.version;
            entityV2.contentId = entityV1.contentId;
            entityV2.createdBy = entityV1.createdBy;
            entityV2.createdOn = entityV1.createdOn;
            // Change versionId to version Order
            entityV2.versionOrder = entityV1.versionId;
            entityV2.description = entityV1.description;
            entityV2.globalId = entityV1.globalId;
            entityV2.labels = entityV1.labels;
            entityV2.name = entityV1.name;
            entityV2.properties = entityV1.properties;
            // isLatest is ignored
            entityV2.artifactId = entityV1.artifactId;
            entityV2.groupId = entityV1.groupId;
            entityV2.state = entityV1.state;

            artifactMap.computeIfAbsent(new GA(entityV2.groupId, entityV2.artifactId), _ignored -> new ArrayList<>()).add(entityV2);
            handler.importEntity(entityV2);
        }


        for (var artifact : artifactMap.values()) {
            var sorted = new ArrayList<>(artifact);
            sorted.sort(Comparator.comparingInt(version -> version.versionOrder));
            int branchOrder = 1;
            for (var version : sorted) {
                var branch = new ArtifactBranchEntity();
                branch.groupId = version.groupId;
                branch.artifactId = version.artifactId;
                branch.version = version.version;
                branch.branchId = BranchId.LATEST.getRawBranchId();
                branch.branchOrder = branchOrder++;
                handler.importEntity(branch);
            }
        }


        for (var entityV1List : artifactRuleMap.values()) {
            for (var entityV1 : entityV1List) {
                var entityV2 = new io.apicurio.registry.impexp.v2.ArtifactRuleEntity();
                entityV2.groupId = entityV1.groupId;
                entityV2.artifactId = entityV1.artifactId;
                entityV2.type = entityV1.type;
                entityV2.configuration = entityV1.configuration;
                handler.importEntity(entityV2);
            }
        }


        for (var entityV1List : commentMap.values()) {
            for (var entityV1 : entityV1List) {
                var entityV2 = new io.apicurio.registry.impexp.v2.CommentEntity();
                entityV2.globalId = entityV1.globalId;
                entityV2.createdBy = entityV1.createdBy;
                entityV2.createdOn = entityV1.createdOn;
                entityV2.commentId = entityV1.commentId; // TODO Make the ID numeric
                entityV2.value = entityV1.value;
                handler.importEntity(entityV2);
            }
        }


        for (var globalRuleEntityV1 : globalRuleMap.values()) {
            var entity2 = new io.apicurio.registry.impexp.v2.GlobalRuleEntity();
            entity2.ruleType = globalRuleEntityV1.ruleType;
            entity2.configuration = globalRuleEntityV1.configuration;
            handler.importEntity(entity2);
        }

        var manifest = new io.apicurio.registry.impexp.v2.ManifestEntity();
        manifest.exportedOn = Instant.now();
        manifest.exportedBy = "2-to-3-migration-utility";
        manifest.systemName = "2-to-3-migration-utility";
        manifest.systemVersion = null; // TODO
        handler.importEntity(manifest);

        handler.postImport();
    }


    @Override
    public void importData(EntityInputStream entities, boolean preserveGlobalId, boolean preserveContentId) {
        log.debug("Calling importData.");
        var utils = new PartialRegistryStorageContentUtils(new ArtifactTypeUtilProviderFactoryImpl());
        DataImporter dataImporter = new SqlDataImporter(log, utils, this, preserveGlobalId, preserveContentId);
        dataImporter.importData(entities, () -> {
        });
    }


    @Override
    public void resetGlobalId() {
        log.debug("Calling resetGlobalId.");
        // NOOP
    }


    @Override
    public void resetContentId() {
        log.debug("Calling resetContentId.");
        // NOOP
    }


    @Override
    public void resetCommentId() {
        log.debug("Calling resetCommentId.");
        // NOOP
    }


    @Override
    public long nextContentId() {
        log.debug("Calling nextContentId.");
        throw new UnsupportedOperationException();
    }


    @Override
    public long nextGlobalId() {
        log.debug("Calling nextGlobalId.");
        throw new UnsupportedOperationException();
    }


    @Override
    public void importComment(CommentEntity entity) {
        log.debug("Calling importComment: {}", entity);
        commentMap.computeIfAbsent(entity.globalId, _ignored -> new ArrayList<>()).add(entity);
    }


    @Override
    public void importGroup(GroupEntity entity) {
        log.debug("Calling importGroup: {}", entity);
        groupEntityMap.put(new GroupId(entity.groupId), entity);
    }


    @Override
    public void importGlobalRule(GlobalRuleEntity entity) {
        log.debug("Calling importGlobalRule: {}", entity);
        globalRuleMap.put(entity.ruleType.name(), entity);
    }


    @Override
    public void importContent(ContentEntity entity) {
        log.debug("Calling importContent: {}", entity);
        contentIdMap.put(entity.contentId, entity);
    }


    @Override
    public void importArtifactVersion(ArtifactVersionEntity entity) {
        log.debug("Calling importArtifactVersion: {}", entity);
        artifactVersionMap.put(new GAV(entity.groupId, entity.artifactId, entity.version), entity);
    }


    @Override
    public void importArtifactRule(ArtifactRuleEntity entity) {
        log.debug("Calling importArtifactRule: {}", entity);
        var ga = new GA(entity.groupId, entity.artifactId);
        artifactRuleMap.computeIfAbsent(ga, _ignored -> new ArrayList<>()).add(entity);
    }


    @Override
    public Map<String, ContentHandle> resolveReferences(List<ArtifactReferenceDto> references) {
        log.debug("Calling resolveReferences: {}", references);
        throw new UnsupportedOperationException();
        //return null;
    }
}
