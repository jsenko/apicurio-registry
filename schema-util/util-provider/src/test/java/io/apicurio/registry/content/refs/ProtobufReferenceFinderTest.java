/*
 * Copyright 2023 Red Hat Inc
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

package io.apicurio.registry.content.refs;

import java.util.Set;

import io.apicurio.registry.schema.refs.ExternalReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.rules.validity.ArtifactUtilProviderTestBase;

/**
 * @author eric.wittmann@gmail.com
 */
public class ProtobufReferenceFinderTest extends ArtifactUtilProviderTestBase {

    /**
     * Test method for {@link io.apicurio.registry.content.refs.AsyncApiReferenceFinder#findExternalReferences(ContentHandle)}.
     */
    @Test
    public void testFindExternalReferences() {
        ContentHandle content = resourceToContentHandle("protobuf-with-refs.proto");
        ProtobufReferenceFinder finder = new ProtobufReferenceFinder();
        Set<ExternalReference> foundReferences = finder.findExternalReferences(content);
        Assertions.assertNotNull(foundReferences);
        Assertions.assertEquals(3, foundReferences.size());
        Assertions.assertEquals(Set.of(
                new ExternalReference("google/protobuf/timestamp.proto"), 
                new ExternalReference("sample/table_info.proto"), 
                new ExternalReference("sample/table_notification_type.proto")), foundReferences);
    }

}
