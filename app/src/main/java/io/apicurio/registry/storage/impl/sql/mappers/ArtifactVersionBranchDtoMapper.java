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

import io.apicurio.registry.storage.dto.BranchDto;
import io.apicurio.registry.storage.impl.sql.jdb.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ArtifactVersionBranchDtoMapper implements RowMapper<BranchDto> {

    public static final ArtifactVersionBranchDtoMapper instance = new ArtifactVersionBranchDtoMapper();


    private ArtifactVersionBranchDtoMapper() {
    }


    @Override
    public BranchDto map(ResultSet rs) throws SQLException {
        return BranchDto.builder()
                .groupId(rs.getString("groupId"))
                .artifactId(rs.getString("artifactId"))
                .branch(rs.getString("branch"))
                .branchOrder(rs.getInt("branchOrder"))
                .version(rs.getString("version"))
                .build();
    }
}
