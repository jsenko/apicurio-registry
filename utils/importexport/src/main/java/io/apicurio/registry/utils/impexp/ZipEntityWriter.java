/*
 * Copyright 2021 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.registry.utils.impexp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.registry.exception.UnreachableCodeException;
import io.apicurio.registry.impexp.Entity;
import io.apicurio.registry.impexp.EntityType;
import io.apicurio.registry.impexp.ImportSink;
import io.apicurio.registry.impexp.v2.*;
import io.apicurio.registry.model.GroupId;

import java.io.IOException;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static io.apicurio.registry.utils.impexp.ZipEntityReader.MAPPER;
import static java.lang.String.format;


public class ZipEntityWriter implements ImportSink {

    private final transient ZipOutputStream zip;


    public ZipEntityWriter(ZipOutputStream zip) {
        this.zip = zip;
    }


    @Override
    public void importEntity(Entity entity) {
        var path = getPath(entity);

        if (entity.getEntityType() == EntityType.CONTENT_V2) {
            var e = (ContentEntity) entity;
            e.base64Content = Base64.getEncoder().encodeToString(e.content.bytes());
            e.content = null;
        }

        var raw = (ObjectNode) MAPPER.valueToTree(entity);
        raw.put("$type", entity.getEntityType().getRawType())
                .put("$version", entity.getEntityType().getVersion());

        try {
            writeEntry(path, raw);
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO
        }
    }


    @Override
    public void postImport() {
        try {
            zip.finish();
        } catch (Exception ex) {
            // TODO
        }
    }


    private String getPath(Entity entity) {
        switch (entity.getEntityType()) {
            case ARTIFACT_RULE_V2: {
                var e = (ArtifactRuleEntity) entity;
                return format("groups/%s/artifacts/%s/rules/%s.json", new GroupId(e.groupId).getRawGroupIdWithDefaultString(), e.artifactId, e.type.name());
            }
            case ARTIFACT_VERSION_V2: {
                var e = (ArtifactVersionEntity) entity;
                return format("groups/%s/artifacts/%s/versions/%s.json", new GroupId(e.groupId).getRawGroupIdWithDefaultString(), e.artifactId, e.version);
            }
            case ARTIFACT_BRANCH_V2: {
                var e = (ArtifactBranchEntity) entity;
                return format("groups/%s/artifacts/%s/branches/%s-%s.json", new GroupId(e.groupId).getRawGroupIdWithDefaultString(), e.artifactId, e.branchId, e.branchOrder);
            }
            case COMMENT_V2: {
                var e = (CommentEntity) entity;
                return format("comments/%s.json", e.commentId);
            }
            case CONTENT_V2: {
                var e = (ContentEntity) entity;
                return format("content/%s.json", e.contentId);
            }
            case GLOBAL_RULE_V2: {
                var e = (GlobalRuleEntity) entity;
                return format("rules/%s.json", e.ruleType.name());
            }
            case GROUP_V2: {
                var e = (GroupEntity) entity;
                return format("groups/%s.json", new GroupId(e.groupId).getRawGroupIdWithDefaultString());
            }
            case MANIFEST_V2: {
                return "manifest.json";
            }
            default:
                throw new UnreachableCodeException();
        }
    }


    private void writeEntry(String path, JsonNode raw) throws IOException {
        var entry = new ZipEntry(path);
        zip.putNextEntry(entry);
        var bytes = MAPPER.writeValueAsBytes(raw);
        zip.write(bytes);
        zip.closeEntry();
    }
}
