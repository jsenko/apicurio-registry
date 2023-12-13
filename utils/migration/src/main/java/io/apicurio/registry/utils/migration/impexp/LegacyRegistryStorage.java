/*
 * Copyright 2020 Red Hat
 * Copyright 2020 IBM
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

package io.apicurio.registry.utils.migration.impexp;

import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.model.ArtifactReferenceDto;
import io.apicurio.registry.utils.migration.impexp.entity.*;

import java.util.List;
import java.util.Map;


public interface LegacyRegistryStorage {


    void importData(EntityInputStream entities, boolean preserveGlobalId, boolean preserveContentId);


    void importComment(CommentEntity entity);


    void importGroup(GroupEntity entity);


    void importGlobalRule(GlobalRuleEntity entity);


    void importContent(ContentEntity entity);


    void importArtifactVersion(ArtifactVersionEntity entity);


    void importArtifactRule(ArtifactRuleEntity entity);


    void resetGlobalId();


    void resetContentId();


    void resetCommentId();


    long nextContentId();


    long nextGlobalId();


    Map<String, ContentHandle> resolveReferences(List<ArtifactReferenceDto> references);
}
