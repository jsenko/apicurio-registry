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

package io.apicurio.registry.storage.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Fabian Martinez
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class GroupMetaDataDto implements Serializable {

    private static final long serialVersionUID = -9015518049780762742L;

    private String groupId;
    private String description;
    private String artifactsType;
    private String createdBy;
    private long createdOn;
    private String modifiedBy;
    private long modifiedOn;
    private Map<String, String> properties;
}
