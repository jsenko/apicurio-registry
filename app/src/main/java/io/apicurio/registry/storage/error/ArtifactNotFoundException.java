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

package io.apicurio.registry.storage.error;

import io.apicurio.registry.model.GroupId;
import lombok.Getter;

/**
 * @author eric.wittmann@gmail.com
 */
public class ArtifactNotFoundException extends NotFoundException {

    private static final long serialVersionUID = -3614783501078800654L;

    @Getter
    private String groupId;

    @Getter
    private String artifactId;


    public ArtifactNotFoundException(String groupId, String artifactId) {
        super(message(groupId, artifactId));
        this.groupId = groupId;
        this.artifactId = artifactId;
    }


    public ArtifactNotFoundException(String groupId, String artifactId, Throwable cause) {
        super(message(groupId, artifactId), cause);
        this.groupId = groupId;
        this.artifactId = artifactId;
    }


    public ArtifactNotFoundException(String artifactId) {
        super(message(GroupId.DEFAULT.getRawGroupIdWithDefaultString(), artifactId));
        this.artifactId = artifactId;
    }


    private static String message(String groupId, String artifactId) {
        return "No artifact with ID '" + artifactId + "' in group '" + groupId + "' was found.";
    }
}
