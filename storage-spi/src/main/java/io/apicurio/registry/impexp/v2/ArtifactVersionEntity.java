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

package io.apicurio.registry.impexp.v2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.apicurio.registry.impexp.Entity;
import io.apicurio.registry.impexp.EntityType;
import io.apicurio.registry.types.ArtifactState;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

/**
 * @author eric.wittmann@gmail.com
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@ToString
@RegisterForReflection
public class ArtifactVersionEntity implements Entity {

    public long globalId;
    public String groupId;
    public String artifactId;
    public String version;

    public int versionOrder;

    public String artifactType;
    public ArtifactState state;
    public String name;
    public String description;
    public String createdBy;
    public long createdOn;
    public List<String> labels;
    public Map<String, String> properties;
    public long contentId;


    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.ARTIFACT_VERSION_V2;
    }
}
