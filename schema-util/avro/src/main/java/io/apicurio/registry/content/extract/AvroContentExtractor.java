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

package io.apicurio.registry.content.extract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.schema.extractor.ContentExtractor;
import io.apicurio.registry.schema.extractor.ExtractedMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Performs meta-data extraction for Avro content.
 *
 * @author Ales Justin
 */
public class AvroContentExtractor implements ContentExtractor {

    Logger log = LoggerFactory.getLogger(getClass());

    private ObjectMapper mapper = new ObjectMapper();

    public AvroContentExtractor() {
    }

    @Override
    public ExtractedMetaData extract(ContentHandle content) {
        ExtractedMetaData metaData = new ExtractedMetaData();
        try {
            JsonNode avroSchema = mapper.readTree(content.bytes());
            JsonNode name = avroSchema.get("name");


            if (name != null && !name.isNull()) {
                metaData.setName(name.asText());
            }
        } catch (IOException ex) {
            log.warn("Error extracting metadata from JSON: {}", ex.getMessage());
        }
        return metaData;
    }
}
