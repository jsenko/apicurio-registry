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

package io.apicurio.registry.storage.dto;

import io.apicurio.registry.types.ArtifactState;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @author eric.wittmann@gmail.com
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ArtifactVersionMetaDataDto {

    private String version;
    private int versionOrder;
    private long globalId;
    private long contentId;
    private String name;
    private String description;
    private String createdBy;
    private long createdOn;
    private String type;
    private ArtifactState state;
    private List<String> labels;
    private Map<String, String> properties;
}
