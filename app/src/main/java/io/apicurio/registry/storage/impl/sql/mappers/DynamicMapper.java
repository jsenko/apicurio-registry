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

import io.apicurio.common.apps.util.Pair;
import io.apicurio.registry.storage.impl.sql.jdb.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.apicurio.registry.storage.impl.sql.mappers.DynamicMapper.DynamicMap;

/**
 * @author Jakub Senko <em>m@jsenko.net</em>
 */
public class DynamicMapper implements RowMapper<DynamicMap> {


    private final Set<Pair<String, Class<?>>> spec;


    private DynamicMapper(Set<Pair<String, Class<?>>> spec) {
        this.spec = spec;
    }


    @Override
    public DynamicMap map(ResultSet rs) throws SQLException {

        var result = new HashMap<Pair<String, Class<?>>, Object>();

        for (Pair<String, Class<?>> key : spec) {
            if (String.class.equals(key.getRight())) {
                result.put(key, rs.getString(key.getLeft()));
            }
            if (Long.class.equals(key.getRight())) {
                result.put(key, rs.getLong(key.getLeft()));
            }
        }
        return new DynamicMap(result);
    }


    public static DynamicMapperBuilder builder() {
        return new DynamicMapperBuilder();
    }


    public static class DynamicMapperBuilder {

        private final Set<Pair<String, Class<?>>> spec = new HashSet<>();


        public DynamicMapperBuilder specify(String column, Class<?> klass) {
            spec.add(Pair.of(column, klass));
            return this;
        }


        public DynamicMapper build() {
            return new DynamicMapper(spec);
        }
    }


    public static class DynamicMap {

        private final Map<Pair<String, Class<?>>, Object> result;


        private DynamicMap(Map<Pair<String, Class<?>>, Object> result) {
            this.result = result;
        }


        public String getString(String column) {
            return (String) result.get(Pair.of(column, String.class));
        }

        public Long getLong(String column) {
            return (Long) result.get(Pair.of(column, Long.class));
        }
    }
}
