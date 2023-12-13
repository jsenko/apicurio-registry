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

package io.apicurio.registry.storage.impl.sql.mappers;

import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.storage.dto.StoredArtifactDto;
import io.apicurio.registry.storage.SqlUtil;
import io.apicurio.registry.storage.impl.sql.jdb.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author eric.wittmann@gmail.com
 */
public class StoredArtifactMapper implements RowMapper<StoredArtifactDto> {

    public static final StoredArtifactMapper instance = new StoredArtifactMapper();

    /**
     * Constructor.
     */
    private StoredArtifactMapper() {
    }

    /**
     * @see io.apicurio.registry.storage.impl.sql.jdb.RowMapper#map(java.sql.ResultSet)
     */
    @Override
    public StoredArtifactDto map(ResultSet rs) throws SQLException {
        return StoredArtifactDto.builder()
                .content(ContentHandle.create(rs.getBytes("content")))
                .contentId(rs.getLong("contentId"))
                .globalId(rs.getLong("globalId"))
                .version(rs.getString("version"))
                .versionOrder(rs.getInt("versionOrder"))
                .references(SqlUtil.deserializeReferences(rs.getString("artifactreferences")))
                .build();
    }
}
