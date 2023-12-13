package io.apicurio.registry.impexp;

import io.apicurio.registry.impexp.v2.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Optional;


@Getter
@ToString(onlyExplicitlyIncluded = true)
public enum EntityType {

    ARTIFACT_RULE_V2("artifact-rule", 2, ArtifactRuleEntity.class),
    ARTIFACT_VERSION_V2("artifact-version", 2, ArtifactVersionEntity.class),
    ARTIFACT_BRANCH_V2("artifact-branch", 2, ArtifactBranchEntity.class),
    COMMENT_V2("comment", 2, CommentEntity.class),
    CONTENT_V2("content", 2, ContentEntity.class),
    GLOBAL_RULE_V2("global-rule", 2, GlobalRuleEntity.class),
    GROUP_V2("group", 2, GroupEntity.class),
    MANIFEST_V2("manifest", 2, ManifestEntity.class),
    ;

    @ToString.Include
    private final String rawType;

    private final int version;

    private final Class<? extends Entity> klass;


    EntityType(String rawType, int version, Class<? extends Entity> klass) {
        this.rawType = rawType;
        this.version = version;
        this.klass = klass;
    }


    public static Optional<EntityType> from(String type, int version) {
        return Arrays.stream(values())
                .filter(t -> t.rawType.equals(type) && t.version == version)
                .findAny();
    }
}
