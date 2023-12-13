/*
 * Copyright 2020 Red Hat Inc
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.registry.model.ArtifactReferenceDto;
import io.apicurio.registry.utils.StringUtil;

import java.util.Collections;
import java.util.List;

/**
 * @author eric.wittmann@gmail.com
 */
public class PartialSqlUtil {

    private static final ObjectMapper mapper = new ObjectMapper();


    /**
     * Deserialize the references from their string form to a List<ArtifactReferenceDto> form.
     *
     * @param references
     */
    public static List<ArtifactReferenceDto> deserializeReferences(String references) {
        try {
            if (StringUtil.isEmpty(references)) {
                return Collections.emptyList();
            }
            return mapper.readValue(references, new TypeReference<List<ArtifactReferenceDto>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
