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

package io.apicurio.registry.utils.impexp;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.impexp.Entity;
import io.apicurio.registry.impexp.EntityType;
import io.apicurio.registry.impexp.v2.ContentEntity;

import java.io.IOException;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ZipEntityReader {

    public static final ObjectMapper MAPPER;

    static {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        MAPPER = new ObjectMapper(jsonFactory);
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private final transient ZipInputStream zip;

    private boolean done;


    public ZipEntityReader(ZipInputStream zip) {
        this.zip = zip;
    }


    public Entity readEntity() {
        try {
            ZipEntry entry = zip.getNextEntry();
            if (entry != null) {
                //var path = entry.getName();
                var entity = readEntry();
                if (entity != null) {
                    if (entity.getEntityType() == EntityType.CONTENT_V2) {
                        var e = (ContentEntity) entity;
                        var bytes = Base64.getDecoder().decode(e.base64Content);
                        e.content = ContentHandle.create(bytes);
                    }
                    return entity;
                } else {
                    return null;
                }
            } else {
                done = true;
                return null;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex); // TODO
        }
    }


    public boolean done() {
        return done;
    }


    private Entity readEntry() {
        try {
            var content = ContentHandle.createNoClose(zip);
            var raw = (ObjectNode) MAPPER.readTree(content.bytes());

            var typeNode = raw.remove("$type");
            var versionNode = raw.remove("$version");

            if (typeNode != null && typeNode.textValue() != null && versionNode != null && versionNode.isNumber()) {

                var type = EntityType.from(typeNode.textValue(), versionNode.intValue());

                if (type.isPresent()) {
                    return MAPPER.treeToValue(raw, type.get().getKlass());
                } else {
                    // TODO
                    return null;
                }
            } else {
                // TODO
                return null;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex); // TODO
        }
    }
}
