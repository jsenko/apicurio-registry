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

import io.apicurio.registry.storage.impl.sql.jdb.RowMapper;
import io.apicurio.registry.model.GAV;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Jakub Senko <em>m@jsenko.net</em>
 */
public class GAVMapper implements RowMapper<GAV> {

    public static final GAVMapper instance = new GAVMapper();


    private GAVMapper() {
    }


    @Override
    public GAV map(ResultSet rs) throws SQLException {
        return new GAV(
                rs.getString("groupId"),
                rs.getString("artifactId"),
                rs.getString("version")
        );
    }
}
