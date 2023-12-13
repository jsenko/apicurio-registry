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
import io.apicurio.registry.model.BranchId;
import io.apicurio.registry.model.GAV;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static lombok.AccessLevel.PRIVATE;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@ToString
@RegisterForReflection
public class ArtifactBranchEntity implements Entity {

    public String groupId;
    public String artifactId;
    public String version;
    public String branchId;
    public int branchOrder;


    public GAV toGAV() {
        return new GAV(groupId, artifactId, version);
    }


    public BranchId toBranchId() {
        return new BranchId(branchId);
    }


    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.ARTIFACT_BRANCH_V2;
    }
}
