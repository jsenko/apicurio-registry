/*
 * Copyright 2020 Red Hat
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

package io.apicurio.registry.storage.impl.kafkasql.values;

import io.apicurio.registry.storage.dto.EditableArtifactMetaDataDto;
import io.apicurio.registry.storage.impl.kafkasql.MessageType;
import io.apicurio.registry.types.ArtifactState;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author eric.wittmann@gmail.com
 */
@RegisterForReflection
@Getter
@Setter
@ToString
public class ArtifactValue extends ArtifactVersionValue {

    private Long globalId;
    private String version;
    private String artifactType;
    private String contentHash;
    private String createdBy;
    private Date createdOn;
    private Integer versionOrder;
    private Long contentId;


    public static ArtifactValue create(ActionType action, Long globalId, String version, String artifactType, String contentHash,
                                       String createdBy, Date createdOn, EditableArtifactMetaDataDto metaData, Integer versionOrder, ArtifactState state, Long contentId) {
        ArtifactValue value = new ArtifactValue();
        value.setAction(action);
        value.setGlobalId(globalId);
        value.setVersion(version);
        value.setArtifactType(artifactType);
        value.setContentHash(contentHash);
        value.setCreatedBy(createdBy);
        value.setCreatedOn(createdOn);
        value.setMetaData(metaData);
        value.setVersionOrder(versionOrder);
        value.setState(state);
        value.setContentId(contentId);
        return value;
    }


    @Override
    public MessageType getType() {
        return MessageType.Artifact;
    }
}
