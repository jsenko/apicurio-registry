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
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;

/**
 * @author eric.wittmann@gmail.com
 */
@RegisterForReflection
public class ManifestEntity implements Entity {

    public String systemVersion;
    public String systemName;
    public Instant exportedOn;
    public String exportedBy;


    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.MANIFEST_V2;
    }
}
