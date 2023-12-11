/*
 * Copyright 2022 Red Hat
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

package io.apicurio.registry.noprofile.storage;

import io.apicurio.common.apps.config.DynamicConfigPropertyDto;
import io.apicurio.registry.AbstractResourceTestBase;
import io.apicurio.registry.content.ContentHandle;
import io.apicurio.registry.model.BranchId;
import io.apicurio.registry.model.GA;
import io.apicurio.registry.model.GAV;
import io.apicurio.registry.storage.RegistryStorage;
import io.apicurio.registry.storage.RegistryStorage.ArtifactRetrievalBehavior;
import io.apicurio.registry.storage.dto.*;
import io.apicurio.registry.storage.error.*;
import io.apicurio.registry.types.ArtifactState;
import io.apicurio.registry.types.ArtifactType;
import io.apicurio.registry.types.RuleType;
import io.apicurio.registry.utils.impexp.EntityType;
import io.apicurio.registry.utils.tests.TestUtils;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.apicurio.registry.storage.RegistryStorage.ArtifactRetrievalBehavior.DEFAULT;
import static io.apicurio.registry.storage.RegistryStorage.ArtifactRetrievalBehavior.SKIP_DISABLED_LATEST;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author eric.wittmann@gmail.com
 */
public abstract class AbstractRegistryStorageTest extends AbstractResourceTestBase {

    private static final String GROUP_ID = AbstractRegistryStorageTest.class.getSimpleName();

    protected static final String OPENAPI_CONTENT = "{" +
            "    \"openapi\": \"3.0.2\"," +
            "    \"info\": {" +
            "        \"title\": \"Empty API\"," +
            "        \"version\": \"1.0.0\"," +
            "        \"description\": \"An example API design using OpenAPI.\"" +
            "    }" +
            "}";
    protected static final String OPENAPI_CONTENT_V2 = "{" +
            "    \"openapi\": \"3.0.2\"," +
            "    \"info\": {" +
            "        \"title\": \"Empty API 2\"," +
            "        \"version\": \"1.0.1\"," +
            "        \"description\": \"An example API design using OpenAPI.\"" +
            "    }" +
            "}";
    protected static final String OPENAPI_CONTENT_TEMPLATE = "{" +
            "    \"openapi\": \"3.0.2\"," +
            "    \"info\": {" +
            "        \"title\": \"Empty API 2\"," +
            "        \"version\": \"VERSION\"," +
            "        \"description\": \"An example API design using OpenAPI.\"" +
            "    }" +
            "}";

    @Inject
    Logger log;

    /**
     * Gets the artifactStore to use.  Subclasses must provide this.
     */
    protected abstract RegistryStorage storage();

    @Test
    public void testGetArtifactIds() throws Exception {

        int size = storage().getArtifactIds(null).size();

        String artifactIdPrefix = "testGetArtifactIds-";
        for (int idx = 1; idx <= 10; idx++) {
            String artifactId = artifactIdPrefix + idx;
            ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
            ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
            assertNotNull(dto);
            assertEquals(GROUP_ID, dto.getGroupId());
            assertEquals(artifactId, dto.getId());

            //Verify group metadata is also created
            GroupMetaDataDto groupMetaDataDto = storage().getGroupMetaData(GROUP_ID);
            assertNotNull(groupMetaDataDto);
            assertEquals(GROUP_ID, groupMetaDataDto.getGroupId());
        }

        int newsize = storage().getArtifactIds(null).size();
        int newids = newsize - size;
        assertEquals(10, newids);
    }

    @Test
    public void testCreateArtifact() throws Exception {
        String artifactId = "testCreateArtifact-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());
        assertEquals("Empty API", dto.getName());
        assertEquals("An example API design using OpenAPI.", dto.getDescription());
        Assertions.assertNull(dto.getLabels());
        Assertions.assertNull(dto.getProperties());
        assertEquals(ArtifactState.ENABLED, dto.getState());
        assertEquals("1.0.0", dto.getVersion());

        StoredArtifactDto storedArtifact = storage().getArtifact(GROUP_ID, artifactId);
        assertNotNull(storedArtifact);
        assertEquals(OPENAPI_CONTENT, storedArtifact.getContent().content());
        assertEquals(dto.getGlobalId(), storedArtifact.getGlobalId());
        assertEquals(dto.getVersion(), storedArtifact.getVersion());

        ArtifactMetaDataDto amdDto = storage().getArtifactMetaData(GROUP_ID, artifactId);
        assertNotNull(amdDto);
        assertEquals(dto.getGlobalId(), amdDto.getGlobalId());
        assertEquals("Empty API", amdDto.getName());
        assertEquals("An example API design using OpenAPI.", amdDto.getDescription());
        assertEquals(ArtifactState.ENABLED, amdDto.getState());
        assertEquals("1.0.0", amdDto.getVersion());
        Assertions.assertNull(amdDto.getLabels());
        Assertions.assertNull(amdDto.getProperties());

        ArtifactVersionMetaDataDto versionMetaDataDto = storage().getArtifactVersionMetaData(GROUP_ID, artifactId, "1.0.0");
        assertNotNull(versionMetaDataDto);
        assertEquals(dto.getGlobalId(), versionMetaDataDto.getGlobalId());
        assertEquals("Empty API", versionMetaDataDto.getName());
        assertEquals("An example API design using OpenAPI.", versionMetaDataDto.getDescription());
        assertEquals(ArtifactState.ENABLED, versionMetaDataDto.getState());
        assertEquals("1.0.0", versionMetaDataDto.getVersion());

        StoredArtifactDto storedVersion = storage().getArtifactVersion(dto.getGlobalId());
        assertNotNull(storedVersion);
        assertEquals(OPENAPI_CONTENT, storedVersion.getContent().content());
        assertEquals(dto.getGlobalId(), storedVersion.getGlobalId());
        assertEquals(dto.getVersion(), storedVersion.getVersion());

        storedVersion = storage().getArtifactVersion(GROUP_ID, artifactId, "1.0.0");
        assertNotNull(storedVersion);
        assertEquals(OPENAPI_CONTENT, storedVersion.getContent().content());
        assertEquals(dto.getGlobalId(), storedVersion.getGlobalId());
        assertEquals(dto.getVersion(), storedVersion.getVersion());

        List<String> versions = storage().getArtifactVersions(GROUP_ID, artifactId);
        assertNotNull(versions);
        assertEquals("1.0.0", versions.iterator().next());
    }

    @Test
    public void testCreateArtifactWithMetaData() throws Exception {
        String artifactId = "testCreateArtifactWithMetaData-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        EditableArtifactMetaDataDto metaData = new EditableArtifactMetaDataDto(
                "NAME", "DESCRIPTION", Collections.singletonList("LABEL-1"), Collections.singletonMap("KEY", "VALUE")
        );
        ArtifactMetaDataDto dto = storage().createArtifactWithMetadata(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, metaData, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());
        assertEquals("NAME", dto.getName());
        assertEquals("DESCRIPTION", dto.getDescription());
        assertNotNull(dto.getLabels());
        assertNotNull(dto.getProperties());
        assertEquals(metaData.getLabels(), dto.getLabels());
        assertEquals(metaData.getProperties(), dto.getProperties());
        assertEquals(ArtifactState.ENABLED, dto.getState());
        assertEquals("1.0.0", dto.getVersion());

        StoredArtifactDto storedArtifact = storage().getArtifact(GROUP_ID, artifactId);
        assertNotNull(storedArtifact);
        assertEquals(OPENAPI_CONTENT, storedArtifact.getContent().content());
        assertEquals(dto.getGlobalId(), storedArtifact.getGlobalId());
        assertEquals(dto.getVersion(), storedArtifact.getVersion());

        ArtifactMetaDataDto amdDto = storage().getArtifactMetaData(GROUP_ID, artifactId);
        assertNotNull(amdDto);
        assertEquals(dto.getGlobalId(), amdDto.getGlobalId());
        assertEquals("NAME", amdDto.getName());
        assertEquals("DESCRIPTION", amdDto.getDescription());
        assertEquals(ArtifactState.ENABLED, amdDto.getState());
        assertEquals("1.0.0", amdDto.getVersion());
        assertEquals(metaData.getLabels(), amdDto.getLabels());
        assertEquals(metaData.getProperties(), amdDto.getProperties());

        // Test creating an artifact with meta-data that is too large for the DB
        artifactId = "testCreateArtifactWithMetaData-2";
        metaData = new EditableArtifactMetaDataDto();
        metaData.setName(generateString(600));
        metaData.setDescription(generateString(2000));
        metaData.setLabels(new ArrayList<>());
        metaData.getLabels().add("label-" + generateString(300));
        metaData.setProperties(new HashMap<>());
        metaData.getProperties().put("key-" + generateString(300), "value-" + generateString(2000));
        dto = storage().createArtifactWithMetadata(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, metaData, null);

        dto = storage().getArtifactMetaData(dto.getGlobalId());
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());
        assertEquals(512, dto.getName().length());
        assertEquals(1024, dto.getDescription().length());
        Assertions.assertTrue(dto.getDescription().endsWith("..."));
        assertNotNull(dto.getLabels());
        assertNotNull(dto.getProperties());
        assertEquals(1, dto.getLabels().size());
        assertEquals(1, dto.getProperties().size());
    }

    @Test
    public void testCreateDuplicateArtifact() throws Exception {
        String artifactId = "testCreateDuplicateArtifact-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);

        // Should throw error for duplicate artifact.
        Assertions.assertThrows(ArtifactAlreadyExistsException.class, () -> {
            storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        });
    }

    @Test
    public void testArtifactNotFound() throws Exception {
        String artifactId = "testArtifactNotFound-1";

        Assertions.assertThrows(ArtifactNotFoundException.class, () -> {
            storage().getArtifact(GROUP_ID, artifactId);
        });

        Assertions.assertThrows(ArtifactNotFoundException.class, () -> {
            storage().getArtifactMetaData(GROUP_ID, artifactId);
        });

        Assertions.assertThrows(ArtifactNotFoundException.class, () -> {
            storage().getArtifactVersion(GROUP_ID, artifactId, "1");
        });

        Assertions.assertThrows(VersionNotFoundException.class, () -> {
            storage().getArtifactVersionMetaData(GROUP_ID, artifactId, "1");
        });
    }

    @Test
    public void testCreateArtifactVersion() throws Exception {
        String artifactId = "testCreateArtifactVersion-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());

        List<String> versions = storage().getArtifactVersions(GROUP_ID, artifactId);
        assertNotNull(versions);
        Assertions.assertFalse(versions.isEmpty());
        assertEquals(1, versions.size());

        ContentHandle contentv2 = ContentHandle.create(OPENAPI_CONTENT_V2);
        ArtifactMetaDataDto dtov2 = storage().updateArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, contentv2, null);
        assertNotNull(dtov2);
        assertEquals(GROUP_ID, dtov2.getGroupId());
        assertEquals(artifactId, dtov2.getId());
        assertEquals("1.0.1", dtov2.getVersion());
        assertEquals(ArtifactState.ENABLED, dtov2.getState());

        versions = storage().getArtifactVersions(GROUP_ID, artifactId);
        assertNotNull(versions);
        Assertions.assertFalse(versions.isEmpty());
        assertEquals(2, versions.size());
    }

    @Test
    public void testGetArtifactVersions() throws Exception {
        String artifactId = "testGetArtifactVersions";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());

        StoredArtifactDto storedArtifact = storage().getArtifact(GROUP_ID, artifactId);
        verifyArtifact(storedArtifact, OPENAPI_CONTENT, dto);

        storedArtifact = storage().getArtifactVersion(GROUP_ID, artifactId, "1.0.0");
        verifyArtifact(storedArtifact, OPENAPI_CONTENT, dto);

        storedArtifact = storage().getArtifactVersion(dto.getGlobalId());
        verifyArtifact(storedArtifact, OPENAPI_CONTENT, dto);

        ArtifactMetaDataDto dtov1 = storage().getArtifactMetaData(dto.getGlobalId());
        verifyArtifactMetadata(dtov1, dto);

        List<String> versions = storage().getArtifactVersions(GROUP_ID, artifactId);
        assertNotNull(versions);
        Assertions.assertFalse(versions.isEmpty());
        assertEquals(1, versions.size());

        ContentHandle contentv2 = ContentHandle.create(OPENAPI_CONTENT_V2);
        ArtifactMetaDataDto dtov2 = storage().updateArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, contentv2, null);
        assertNotNull(dtov2);
        assertEquals(GROUP_ID, dtov2.getGroupId());
        assertEquals(artifactId, dtov2.getId());
        assertEquals("1.0.1", dtov2.getVersion());
        assertEquals(ArtifactState.ENABLED, dtov2.getState());

        versions = storage().getArtifactVersions(GROUP_ID, artifactId);
        assertNotNull(versions);
        Assertions.assertFalse(versions.isEmpty());
        assertEquals(2, versions.size());

        //verify version 2

        storedArtifact = storage().getArtifact(GROUP_ID, artifactId);
        verifyArtifact(storedArtifact, OPENAPI_CONTENT_V2, dtov2);

        storedArtifact = storage().getArtifactVersion(GROUP_ID, artifactId, "1.0.1");
        verifyArtifact(storedArtifact, OPENAPI_CONTENT_V2, dtov2);

        storedArtifact = storage().getArtifactVersion(dtov2.getGlobalId());
        verifyArtifact(storedArtifact, OPENAPI_CONTENT_V2, dtov2);

        ArtifactMetaDataDto dtov2Stored = storage().getArtifactMetaData(dtov2.getGlobalId());
        verifyArtifactMetadata(dtov2Stored, dtov2);

        // verify version 1 again

        storedArtifact = storage().getArtifactVersion(GROUP_ID, artifactId, "1.0.0");
        verifyArtifact(storedArtifact, OPENAPI_CONTENT, dto);

        storedArtifact = storage().getArtifactVersion(dto.getGlobalId());
        verifyArtifact(storedArtifact, OPENAPI_CONTENT, dto);

        dtov1 = storage().getArtifactMetaData(dto.getGlobalId());
        verifyArtifactMetadata(dtov1, dto);

    }

    private void verifyArtifact(StoredArtifactDto storedArtifact, String content, ArtifactMetaDataDto expectedMetadata) {
        assertNotNull(storedArtifact);
        assertEquals(content, storedArtifact.getContent().content());
        assertEquals(expectedMetadata.getGlobalId(), storedArtifact.getGlobalId());
        assertEquals(expectedMetadata.getVersion(), storedArtifact.getVersion());
    }

    private void verifyArtifactMetadata(ArtifactMetaDataDto actualMetadata, ArtifactMetaDataDto expectedMetadata) {
        assertNotNull(actualMetadata);
        assertNotNull(expectedMetadata);
        assertEquals(expectedMetadata.getGlobalId(), actualMetadata.getGlobalId());
        assertEquals(expectedMetadata.getVersion(), actualMetadata.getVersion());
    }

    @Test
    public void testCreateArtifactVersionWithMetaData() throws Exception {
        String artifactId = "testCreateArtifactVersionWithMetaData-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());

        List<String> versions = storage().getArtifactVersions(GROUP_ID, artifactId);
        assertNotNull(versions);
        Assertions.assertFalse(versions.isEmpty());
        assertEquals(1, versions.size());

        ContentHandle contentv2 = ContentHandle.create(OPENAPI_CONTENT_V2);
        EditableArtifactMetaDataDto metaData = new EditableArtifactMetaDataDto("NAME", "DESC", Collections.singletonList("LBL"), Collections.singletonMap("K", "V"));
        ArtifactMetaDataDto dtov2 = storage().updateArtifactWithMetadata(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, contentv2, metaData, null);
        assertNotNull(dtov2);
        assertEquals(GROUP_ID, dtov2.getGroupId());
        assertEquals(artifactId, dtov2.getId());
        assertEquals("1.0.1", dtov2.getVersion());
        assertEquals(ArtifactState.ENABLED, dtov2.getState());
        assertEquals("NAME", dtov2.getName());
        assertEquals("DESC", dtov2.getDescription());
        assertEquals(metaData.getLabels(), dtov2.getLabels());
        assertEquals(metaData.getProperties(), dtov2.getProperties());

        versions = storage().getArtifactVersions(GROUP_ID, artifactId);
        assertNotNull(versions);
        Assertions.assertFalse(versions.isEmpty());
        assertEquals(2, versions.size());

        ArtifactVersionMetaDataDto vmd = storage().getArtifactVersionMetaData(GROUP_ID, artifactId, "1.0.1");
        assertNotNull(vmd);
        assertEquals("NAME", vmd.getName());
        assertEquals("DESC", vmd.getDescription());
    }

    @Test
    public void testGetArtifactMetaDataByGlobalId() throws Exception {
        String artifactId = "testGetArtifactMetaDataByGlobalId-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());
        assertEquals("Empty API", dto.getName());
        assertEquals("An example API design using OpenAPI.", dto.getDescription());
        Assertions.assertNull(dto.getLabels());
        Assertions.assertNull(dto.getProperties());
        assertEquals(ArtifactState.ENABLED, dto.getState());
        assertEquals("1.0.0", dto.getVersion());

        long globalId = dto.getGlobalId();

        dto = storage().getArtifactMetaData(globalId);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());
        assertEquals("Empty API", dto.getName());
        assertEquals("An example API design using OpenAPI.", dto.getDescription());
        Assertions.assertNull(dto.getLabels());
        Assertions.assertNull(dto.getProperties());
        assertEquals(ArtifactState.ENABLED, dto.getState());
        assertEquals("1.0.0", dto.getVersion());
    }

    @Test
    public void testUpdateArtifactMetaData() throws Exception {
        String artifactId = "testUpdateArtifactMetaData-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());
        assertEquals("Empty API", dto.getName());
        assertEquals("An example API design using OpenAPI.", dto.getDescription());
        Assertions.assertNull(dto.getLabels());
        Assertions.assertNull(dto.getProperties());
        assertEquals(ArtifactState.ENABLED, dto.getState());
        assertEquals("1.0.0", dto.getVersion());

        String newName = "Updated Name";
        String newDescription = "Updated description.";
        List<String> newLabels = Collections.singletonList("foo");
        Map<String, String> newProperties = new HashMap<>();
        newProperties.put("foo", "bar");
        newProperties.put("ting", "bin");
        EditableArtifactMetaDataDto emd = new EditableArtifactMetaDataDto(newName, newDescription, newLabels, newProperties);
        storage().updateArtifactMetaData(GROUP_ID, artifactId, emd);

        ArtifactMetaDataDto metaData = storage().getArtifactMetaData(GROUP_ID, artifactId);
        assertNotNull(metaData);
        assertEquals(newName, metaData.getName());
        assertEquals(newDescription, metaData.getDescription());
        assertEquals(newLabels, metaData.getLabels());
        assertEquals(newProperties, metaData.getProperties());
    }

    @Test
    public void testUpdateArtifactState() throws Exception {
        String artifactId = "testUpdateArtifactState-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(ArtifactState.ENABLED, dto.getState());

        storage().updateArtifactState(GROUP_ID, artifactId, ArtifactState.DEPRECATED);

        ArtifactMetaDataDto metaData = storage().getArtifactMetaData(GROUP_ID, artifactId);
        assertNotNull(metaData);
        assertEquals(ArtifactState.DEPRECATED, metaData.getState());
    }

    @Test
    public void testUpdateArtifactVersionState() throws Exception {
        String artifactId = "testUpdateArtifactVersionState-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(ArtifactState.ENABLED, dto.getState());

        ContentHandle contentv2 = ContentHandle.create(OPENAPI_CONTENT_V2);
        ArtifactMetaDataDto dtov2 = storage().updateArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, contentv2, null);
        assertNotNull(dtov2);
        assertEquals(GROUP_ID, dtov2.getGroupId());
        assertEquals(artifactId, dtov2.getId());
        assertEquals("1.0.1", dtov2.getVersion());
        assertEquals(ArtifactState.ENABLED, dtov2.getState());

        storage().updateArtifactState(GROUP_ID, artifactId, "1.0.0", ArtifactState.DISABLED);
        storage().updateArtifactState(GROUP_ID, artifactId, "1.0.1", ArtifactState.DEPRECATED);

        ArtifactVersionMetaDataDto v1 = storage().getArtifactVersionMetaData(GROUP_ID, artifactId, "1.0.0");
        ArtifactVersionMetaDataDto v2 = storage().getArtifactVersionMetaData(GROUP_ID, artifactId, "1.0.1");
        assertNotNull(v1);
        assertNotNull(v2);
        assertEquals(ArtifactState.DISABLED, v1.getState());
        assertEquals(ArtifactState.DEPRECATED, v2.getState());
    }

    @Test
    public void testUpdateArtifactVersionMetaData() throws Exception {
        String artifactId = "testUpdateArtifactVersionMetaData-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());
        assertEquals("Empty API", dto.getName());
        assertEquals("An example API design using OpenAPI.", dto.getDescription());
        Assertions.assertNull(dto.getLabels());
        Assertions.assertNull(dto.getProperties());
        assertEquals(ArtifactState.ENABLED, dto.getState());
        assertEquals("1.0.0", dto.getVersion());

        String newName = "Updated Name";
        String newDescription = "Updated description.";
        List<String> newLabels = Collections.singletonList("foo");
        Map<String, String> newProperties = new HashMap<>();
        newProperties.put("foo", "bar");
        newProperties.put("ting", "bin");
        EditableArtifactMetaDataDto emd = new EditableArtifactMetaDataDto(newName, newDescription, newLabels, newProperties);
        storage().updateArtifactVersionMetaData(GROUP_ID, artifactId, "1.0.0", emd);

        ArtifactVersionMetaDataDto metaData = storage().getArtifactVersionMetaData(GROUP_ID, artifactId, "1.0.0");
        assertNotNull(metaData);
        assertEquals(newName, metaData.getName());
        assertEquals(newDescription, metaData.getDescription());
    }

    @Test
    public void testDeleteArtifact() throws Exception {
        String artifactId = "testDeleteArtifact-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());
        assertEquals("Empty API", dto.getName());
        assertEquals("An example API design using OpenAPI.", dto.getDescription());
        Assertions.assertNull(dto.getLabels());
        Assertions.assertNull(dto.getProperties());
        assertEquals(ArtifactState.ENABLED, dto.getState());
        assertEquals("1.0.0", dto.getVersion());

        storage().getArtifact(GROUP_ID, artifactId);

        storage().deleteArtifact(GROUP_ID, artifactId);

        Assertions.assertThrows(ArtifactNotFoundException.class, () -> {
            storage().getArtifact(GROUP_ID, artifactId);
        });
        Assertions.assertThrows(ArtifactNotFoundException.class, () -> {
            storage().getArtifactMetaData(GROUP_ID, artifactId);
        });
        Assertions.assertThrows(ArtifactNotFoundException.class, () -> {
            storage().getArtifactVersion(GROUP_ID, artifactId, "1.0.0");
        });
        Assertions.assertThrows(VersionNotFoundException.class, () -> {
            storage().getArtifactVersionMetaData(GROUP_ID, artifactId, "1.0.0");
        });
    }

    @Test
    public void testDeleteArtifactVersion() throws Exception {
        // Delete the only version
        ////////////////////////////
        String artifactId = "testDeleteArtifactVersion-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(ArtifactState.ENABLED, dto.getState());
        assertEquals("1.0.0", dto.getVersion());

        storage().deleteArtifactVersion(GROUP_ID, artifactId, "1.0.0");

        final String aid1 = artifactId;
        Assertions.assertThrows(ArtifactNotFoundException.class, () -> {
            storage().getArtifact(GROUP_ID, aid1);
        });
        Assertions.assertThrows(ArtifactNotFoundException.class, () -> {
            storage().getArtifactMetaData(GROUP_ID, aid1);
        });
        Assertions.assertThrows(ArtifactNotFoundException.class, () -> {
            storage().getArtifactVersion(GROUP_ID, aid1, "1.0.0");
        });
        Assertions.assertThrows(VersionNotFoundException.class, () -> {
            storage().getArtifactVersionMetaData(GROUP_ID, aid1, "1.0.0");
        });

        // Delete one of multiple versions
        artifactId = "testDeleteArtifactVersion-2";
        content = ContentHandle.create(OPENAPI_CONTENT);
        dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(ArtifactState.ENABLED, dto.getState());
        assertEquals("1.0.0", dto.getVersion());

        ContentHandle contentv2 = ContentHandle.create(OPENAPI_CONTENT_V2);
        ArtifactMetaDataDto dtov2 = storage().updateArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, contentv2, null);
        assertNotNull(dtov2);
        assertEquals("1.0.1", dtov2.getVersion());

        storage().deleteArtifactVersion(GROUP_ID, artifactId, "1.0.0");

        final String aid2 = artifactId;

        storage().getArtifact(GROUP_ID, aid2);
        storage().getArtifactMetaData(GROUP_ID, aid2);
        Assertions.assertThrows(ArtifactNotFoundException.class, () -> {
            storage().getArtifactVersion(GROUP_ID, aid2, "1.0.0");
        });
        Assertions.assertThrows(VersionNotFoundException.class, () -> {
            storage().getArtifactVersionMetaData(GROUP_ID, aid2, "1.0.0");
        });

        ArtifactMetaDataDto dtov3 = storage().updateArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dtov3);
        assertEquals("1.0.0", dtov3.getVersion());

        // Update version 2 to DISABLED state and delete latest version
        storage().updateArtifactState(GROUP_ID, artifactId, "1.0.1", ArtifactState.DISABLED);
        storage().deleteArtifactVersion(GROUP_ID, artifactId, "1.0.0");

        ArtifactMetaDataDto artifactMetaData = storage().getArtifactMetaData(GROUP_ID, aid2, DEFAULT);
        assertNotNull(artifactMetaData);
        assertEquals("1.0.1", artifactMetaData.getVersion());
        assertEquals(aid2, artifactMetaData.getId());
        assertEquals(ArtifactState.DISABLED, artifactMetaData.getState());

        // Delete the latest version
        artifactId = "testDeleteArtifactVersion-3";
        content = ContentHandle.create(OPENAPI_CONTENT);
        dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(ArtifactState.ENABLED, dto.getState());
        assertEquals("1.0.0", dto.getVersion());

        contentv2 = ContentHandle.create(OPENAPI_CONTENT_V2);
        dtov2 = storage().updateArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, contentv2, null);
        assertNotNull(dtov2);

        final String aid3 = artifactId;
        storage().deleteArtifactVersion(GROUP_ID, aid3, "1.0.1");
        List<String> versions = storage().getArtifactVersions(GROUP_ID, aid3);
        assertNotNull(versions);
        Assertions.assertFalse(versions.isEmpty());
        assertEquals(1, versions.size());
        assertEquals("1.0.0", versions.iterator().next());

        VersionSearchResultsDto result = storage().searchVersions(GROUP_ID, aid3, 0, 10);
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assertEquals("1.0.0", result.getVersions().iterator().next().getVersion());

        artifactMetaData = storage().getArtifactMetaData(GROUP_ID, aid3);
        assertNotNull(artifactMetaData);
        assertEquals("1.0.0", artifactMetaData.getVersion());
        assertEquals(aid3, artifactMetaData.getId());

        storage().getArtifact(GROUP_ID, aid3);
        ArtifactMetaDataDto metaData = storage().getArtifactMetaData(GROUP_ID, aid3);
        assertNotNull(metaData);
        assertEquals("1.0.0", metaData.getVersion());
        Assertions.assertThrows(ArtifactNotFoundException.class, () -> {
            storage().getArtifactVersion(GROUP_ID, aid3, "1.0.1");
        });
        Assertions.assertThrows(VersionNotFoundException.class, () -> {
            storage().getArtifactVersionMetaData(GROUP_ID, aid3, "1.0.1");
        });

        // Delete the only artifact version left - same as deleting the whole artifact
        storage().deleteArtifactVersion(GROUP_ID, aid3, "1.0.0");
        Assertions.assertThrows(ArtifactNotFoundException.class, () -> {
            storage().getArtifact(GROUP_ID, aid3, DEFAULT);
        });
    }

    @Test
    public void testDeleteArtifactVersionMetaData() throws Exception {
        String artifactId = "testDeleteArtifactVersionMetaData-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());
        assertEquals("Empty API", dto.getName());
        assertEquals("An example API design using OpenAPI.", dto.getDescription());
        Assertions.assertNull(dto.getLabels());
        Assertions.assertNull(dto.getProperties());
        assertEquals(ArtifactState.ENABLED, dto.getState());
        assertEquals("1.0.0", dto.getVersion());

        storage().deleteArtifactVersionMetaData(GROUP_ID, artifactId, "1.0.0");

        ArtifactVersionMetaDataDto metaData = storage().getArtifactVersionMetaData(GROUP_ID, artifactId, "1.0.0");
        assertNotNull(metaData);
        Assertions.assertNull(metaData.getName());
        Assertions.assertNull(metaData.getDescription());
        assertEquals(ArtifactState.ENABLED, metaData.getState());
        assertEquals("1.0.0", metaData.getVersion());
    }

    @Test
    public void testCreateArtifactRule() throws Exception {
        String artifactId = "testCreateArtifactRule-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());

        List<RuleType> artifactRules = storage().getArtifactRules(GROUP_ID, artifactId);
        assertNotNull(artifactRules);
        Assertions.assertTrue(artifactRules.isEmpty());

        RuleConfigurationDto configDto = new RuleConfigurationDto("FULL");
        storage().createArtifactRule(GROUP_ID, artifactId, RuleType.VALIDITY, configDto);

        artifactRules = storage().getArtifactRules(GROUP_ID, artifactId);
        assertNotNull(artifactRules);
        Assertions.assertFalse(artifactRules.isEmpty());
        assertEquals(1, artifactRules.size());
        assertEquals(RuleType.VALIDITY, artifactRules.get(0));
    }

    @Test
    public void testUpdateArtifactRule() throws Exception {
        String artifactId = "testUpdateArtifactRule-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());

        RuleConfigurationDto configDto = new RuleConfigurationDto("FULL");
        storage().createArtifactRule(GROUP_ID, artifactId, RuleType.VALIDITY, configDto);

        RuleConfigurationDto rule = storage().getArtifactRule(GROUP_ID, artifactId, RuleType.VALIDITY);
        assertNotNull(rule);
        assertEquals("FULL", rule.getConfiguration());

        RuleConfigurationDto updatedConfig = new RuleConfigurationDto("NONE");
        storage().updateArtifactRule(GROUP_ID, artifactId, RuleType.VALIDITY, updatedConfig);

        rule = storage().getArtifactRule(GROUP_ID, artifactId, RuleType.VALIDITY);
        assertNotNull(rule);
        assertEquals("NONE", rule.getConfiguration());
    }

    @Test
    public void testDeleteArtifactRule() throws Exception {
        String artifactId = "testDeleteArtifactRule-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());

        RuleConfigurationDto configDto = new RuleConfigurationDto("FULL");
        storage().createArtifactRule(GROUP_ID, artifactId, RuleType.VALIDITY, configDto);

        RuleConfigurationDto rule = storage().getArtifactRule(GROUP_ID, artifactId, RuleType.VALIDITY);
        assertNotNull(rule);
        assertEquals("FULL", rule.getConfiguration());

        storage().deleteArtifactRule(GROUP_ID, artifactId, RuleType.VALIDITY);

        Assertions.assertThrows(RuleNotFoundException.class, () -> {
            storage().getArtifactRule(GROUP_ID, artifactId, RuleType.VALIDITY);
        });
    }

    @Test
    public void testDeleteAllArtifactRules() throws Exception {
        String artifactId = "testDeleteAllArtifactRulse-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());

        RuleConfigurationDto configDto = new RuleConfigurationDto("FULL");
        storage().createArtifactRule(GROUP_ID, artifactId, RuleType.VALIDITY, configDto);
        storage().createArtifactRule(GROUP_ID, artifactId, RuleType.COMPATIBILITY, configDto);

        List<RuleType> rules = storage().getArtifactRules(GROUP_ID, artifactId);
        assertEquals(2, rules.size());

        storage().deleteArtifactRules(GROUP_ID, artifactId);

        Assertions.assertThrows(RuleNotFoundException.class, () -> {
            storage().getArtifactRule(GROUP_ID, artifactId, RuleType.VALIDITY);
        });
        Assertions.assertThrows(RuleNotFoundException.class, () -> {
            storage().getArtifactRule(GROUP_ID, artifactId, RuleType.COMPATIBILITY);
        });
    }

    @Test
    public void testGlobalRules() {
        List<RuleType> globalRules = storage().getGlobalRules();
        assertNotNull(globalRules);
        Assertions.assertTrue(globalRules.isEmpty());

        RuleConfigurationDto config = new RuleConfigurationDto();
        config.setConfiguration("FULL");
        storage().createGlobalRule(RuleType.COMPATIBILITY, config);

        RuleConfigurationDto rule = storage().getGlobalRule(RuleType.COMPATIBILITY);
        assertEquals(rule.getConfiguration(), config.getConfiguration());

        globalRules = storage().getGlobalRules();
        assertNotNull(globalRules);
        Assertions.assertFalse(globalRules.isEmpty());
        assertEquals(globalRules.size(), 1);
        assertEquals(globalRules.get(0), RuleType.COMPATIBILITY);

        Assertions.assertThrows(RuleAlreadyExistsException.class, () -> {
            storage().createGlobalRule(RuleType.COMPATIBILITY, config);
        });

        RuleConfigurationDto updatedConfig = new RuleConfigurationDto("FORWARD");
        storage().updateGlobalRule(RuleType.COMPATIBILITY, updatedConfig);

        rule = storage().getGlobalRule(RuleType.COMPATIBILITY);
        assertEquals(rule.getConfiguration(), updatedConfig.getConfiguration());

        Assertions.assertThrows(RuleNotFoundException.class, () -> {
            storage().updateGlobalRule(RuleType.VALIDITY, config);
        });

        storage().deleteGlobalRules();
        globalRules = storage().getGlobalRules();
        assertNotNull(globalRules);
        Assertions.assertTrue(globalRules.isEmpty());

        storage().createGlobalRule(RuleType.COMPATIBILITY, config);
        storage().deleteGlobalRule(RuleType.COMPATIBILITY);
        globalRules = storage().getGlobalRules();
        assertNotNull(globalRules);
        Assertions.assertTrue(globalRules.isEmpty());
    }

    @Test
    public void testSearchArtifacts() throws Exception {
        String artifactIdPrefix = "testSearchArtifacts-";
        for (int idx = 1; idx <= 50; idx++) {
            String idxs = (idx < 10 ? "0" : "") + idx;
            String artifactId = artifactIdPrefix + idxs;
            ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
            List<String> labels = Collections.singletonList("label-" + idx);
            Map<String, String> properties = Collections.singletonMap("key", "value-" + idx);
            EditableArtifactMetaDataDto metaData = new EditableArtifactMetaDataDto(
                    artifactId + "-name",
                    artifactId + "-description",
                    labels,
                    properties);
            storage().createArtifactWithMetadata(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, metaData, null);
        }

        long start = System.currentTimeMillis();

        Set<SearchFilter> filters = Collections.singleton(SearchFilter.ofName("testSearchArtifacts"));
        ArtifactSearchResultsDto results = storage().searchArtifacts(filters, OrderBy.name, OrderDirection.asc, 0, 10);
        assertNotNull(results);
        assertEquals(50, results.getCount());
        assertNotNull(results.getArtifacts());
        assertEquals(10, results.getArtifacts().size());


        filters = Collections.singleton(SearchFilter.ofName("testSearchArtifacts-19-name"));
        results = storage().searchArtifacts(filters, OrderBy.name, OrderDirection.asc, 0, 10);
        assertNotNull(results);
        assertEquals(1, results.getCount());
        assertNotNull(results.getArtifacts());
        assertEquals(1, results.getArtifacts().size());
        assertEquals("testSearchArtifacts-19-name", results.getArtifacts().get(0).getName());


        filters = Collections.singleton(SearchFilter.ofDescription("testSearchArtifacts-33-description"));
        results = storage().searchArtifacts(filters, OrderBy.name, OrderDirection.asc, 0, 10);
        assertNotNull(results);
        assertEquals(1, results.getCount());
        assertNotNull(results.getArtifacts());
        assertEquals(1, results.getArtifacts().size());
        assertEquals("testSearchArtifacts-33-name", results.getArtifacts().get(0).getName());


        filters = Collections.emptySet();
        results = storage().searchArtifacts(filters, OrderBy.name, OrderDirection.asc, 0, 10);
        assertNotNull(results);
        assertNotNull(results.getArtifacts());
        assertEquals(10, results.getArtifacts().size());


        filters = Collections.singleton(SearchFilter.ofEverything("testSearchArtifacts"));
        results = storage().searchArtifacts(filters, OrderBy.name, OrderDirection.asc, 0, 1000);
        assertNotNull(results);
        assertEquals(50, results.getCount());
        assertNotNull(results.getArtifacts());
        assertEquals(50, results.getArtifacts().size());
        assertEquals("testSearchArtifacts-01-name", results.getArtifacts().get(0).getName());
        assertEquals("testSearchArtifacts-02-name", results.getArtifacts().get(1).getName());


        filters = Collections.singleton(SearchFilter.ofLabel("label-17"));
        results = storage().searchArtifacts(filters, OrderBy.name, OrderDirection.asc, 0, 10);
        assertNotNull(results);
        assertEquals(1, results.getCount());
        assertNotNull(results.getArtifacts());
        assertEquals(1, results.getArtifacts().size());
        assertEquals("testSearchArtifacts-17-name", results.getArtifacts().get(0).getName());


        filters = Collections.singleton(SearchFilter.ofEverything("label-17"));
        results = storage().searchArtifacts(filters, OrderBy.name, OrderDirection.asc, 0, 10);
        assertNotNull(results);
        assertEquals(1, results.getCount());
        assertNotNull(results.getArtifacts());
        assertEquals(1, results.getArtifacts().size());
        assertEquals("testSearchArtifacts-17-name", results.getArtifacts().get(0).getName());

        long end = System.currentTimeMillis();
        System.out.println("Search time: " + (end - start) + "ms");
    }

    @Test
    public void testSearchVersions() throws Exception {
        String artifactId = "testSearchVersions-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());

        // Add more versions
        for (int idx = 2; idx <= 50; idx++) {
            content = ContentHandle.create(OPENAPI_CONTENT_TEMPLATE.replaceAll("VERSION", "1.0." + idx));
            EditableArtifactMetaDataDto metaData = new EditableArtifactMetaDataDto(
                    artifactId + "-name-" + idx,
                    artifactId + "-description-" + idx,
                    null,
                    null);
            storage().updateArtifactWithMetadata(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, metaData, null);
        }

        TestUtils.retry(() -> {
            VersionSearchResultsDto results = storage().searchVersions(GROUP_ID, artifactId, 0, 10);
            assertNotNull(results);
            assertEquals(50, results.getCount());
            assertEquals(10, results.getVersions().size());

            results = storage().searchVersions(GROUP_ID, artifactId, 0, 1000);
            assertNotNull(results);
            assertEquals(50, results.getCount());
            assertEquals(50, results.getVersions().size());
        });
    }

    private void createSomeUserData() {
        final String group1 = "testGroup-1";
        final String group2 = "testGroup-2";
        final String artifactId1 = "testArtifact-1";
        final String artifactId2 = "testArtifact-2";
        final String principal = "testPrincipal";
        final String role = "testRole";

        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        // storage().createGroup(GroupMetaDataDto.builder().groupId(group1).build());
        // ^ TODO Uncomment after https://github.com/Apicurio/apicurio-registry/issues/1721
        ArtifactMetaDataDto artifactDto1 = storage().createArtifact(group1, artifactId1, null, ArtifactType.OPENAPI, content, null);
        storage().createArtifactRule(group1, artifactId1, RuleType.VALIDITY, RuleConfigurationDto.builder().configuration("FULL").build());
        ArtifactMetaDataDto artifactDto2 = storage().createArtifactWithMetadata(
                group2, artifactId2, null, ArtifactType.OPENAPI, content, EditableArtifactMetaDataDto.builder().name("test").build(), null);
        storage().createGlobalRule(RuleType.VALIDITY, RuleConfigurationDto.builder().configuration("FULL").build());
        storage().createRoleMapping(principal, role, null);

        // Verify data exists

        assertNotNull(storage().getArtifactVersion(group1, artifactId1, artifactDto1.getVersion()));
        assertEquals(1, storage().getArtifactRules(group1, artifactId1).size());
        assertNotNull(storage().getArtifactVersion(group2, artifactId2, artifactDto2.getVersion()));
        assertEquals(1, storage().getGlobalRules().size());
        assertEquals(role, storage().getRoleForPrincipal(principal));
    }

    private int countStorageEntities() {
        // We don't need thread safety, but it's simpler to use this when effectively final counter is needed
        final AtomicInteger count = new AtomicInteger(0);
        storage().exportData(e -> {
            if (e.getEntityType() != EntityType.Manifest) {
                log.debug("Counting from export: {}", e);
                count.incrementAndGet();
            }
            return null;
        });
        int res = count.get();
        // Count data that is not exported
        res += storage().getRoleMappings().size();
        return res;
    }

    @Test
    public void testDeleteAllUserData() {
        // Delete first to cleanup after other tests
        storage().deleteAllUserData();
        createSomeUserData();
        assertEquals(8, countStorageEntities());
        // ^ TODO Change to 9 after https://github.com/Apicurio/apicurio-registry/issues/1721
        // Delete all
        storage().deleteAllUserData();
        assertEquals(0, countStorageEntities());
    }

    @Test
    public void testConfigProperties() throws Exception {
        List<DynamicConfigPropertyDto> properties = storage().getConfigProperties();
        assertNotNull(properties);
        Assertions.assertTrue(properties.isEmpty());

        storage().setConfigProperty(new DynamicConfigPropertyDto("registry.test.property-string", "test-value"));
        storage().setConfigProperty(new DynamicConfigPropertyDto("registry.test.property-boolean", "true"));
        storage().setConfigProperty(new DynamicConfigPropertyDto("registry.test.property-long", "12345"));

        properties = storage().getConfigProperties();
        assertNotNull(properties);
        Assertions.assertFalse(properties.isEmpty());
        assertEquals(3, properties.size());

        DynamicConfigPropertyDto stringProp = getProperty(properties, "registry.test.property-string");
        DynamicConfigPropertyDto boolProp = getProperty(properties, "registry.test.property-boolean");
        DynamicConfigPropertyDto longProp = getProperty(properties, "registry.test.property-long");

        assertNotNull(stringProp);
        assertNotNull(boolProp);
        assertNotNull(longProp);

        assertEquals("test-value", stringProp.getValue());
        assertEquals("true", boolProp.getValue());
        assertEquals("12345", longProp.getValue());
    }

    private DynamicConfigPropertyDto getProperty(List<DynamicConfigPropertyDto> properties, String propertyName) {
        for (DynamicConfigPropertyDto prop : properties) {
            if (prop.getName().equals(propertyName)) {
                return prop;
            }
        }
        return null;
    }

    @Test
    public void testComments() throws Exception {
        String artifactId = "testComments-1";
        ContentHandle content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dto = storage().createArtifact(GROUP_ID, artifactId, null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dto);
        assertEquals(GROUP_ID, dto.getGroupId());
        assertEquals(artifactId, dto.getId());

        List<CommentDto> comments = storage().getArtifactVersionComments(GROUP_ID, artifactId, dto.getVersion());
        Assertions.assertTrue(comments.isEmpty());

        storage().createArtifactVersionComment(GROUP_ID, artifactId, dto.getVersion(), "TEST_COMMENT_1");
        storage().createArtifactVersionComment(GROUP_ID, artifactId, dto.getVersion(), "TEST_COMMENT_2");
        storage().createArtifactVersionComment(GROUP_ID, artifactId, dto.getVersion(), "TEST_COMMENT_3");

        comments = storage().getArtifactVersionComments(GROUP_ID, artifactId, dto.getVersion());
        assertEquals(3, comments.size());

        storage().deleteArtifactVersionComment(GROUP_ID, artifactId, dto.getVersion(), comments.get(1).getCommentId());

        comments = storage().getArtifactVersionComments(GROUP_ID, artifactId, dto.getVersion());
        assertEquals(2, comments.size());

        storage().updateArtifactVersionComment(GROUP_ID, artifactId, dto.getVersion(), comments.get(0).getCommentId(), "TEST_COMMENT_4");

        comments = storage().getArtifactVersionComments(GROUP_ID, artifactId, dto.getVersion());
        assertEquals(2, comments.size());
        assertEquals("TEST_COMMENT_4", comments.get(0).getValue());
    }


    @Test
    public void testBranches() {

        var ga = new GA(GROUP_ID, "foo");

        assertThrows(ArtifactNotFoundException.class, () -> storage().getArtifactBranches(ga));

        var content = ContentHandle.create(OPENAPI_CONTENT);
        ArtifactMetaDataDto dtoV1 = storage().createArtifact(GROUP_ID, ga.getRawArtifactId(), null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dtoV1);
        assertEquals(ga.getRawGroupIdWithDefaultString(), dtoV1.getGroupId());
        assertEquals(ga.getRawArtifactId(), dtoV1.getId());

        var branches = storage().getArtifactBranches(ga);
        assertEquals(Map.of(BranchId.LATEST, List.of(new GAV(ga, dtoV1.getVersion()))), branches);

        var latestBranch = storage().getArtifactBranch(ga, BranchId.LATEST, DEFAULT);
        assertEquals(List.of(new GAV(ga, dtoV1.getVersion())), latestBranch);

        var gavV1 = storage().getArtifactBranchLeaf(ga, BranchId.LATEST, DEFAULT);
        assertNotNull(gavV1);
        assertEquals(gavV1.getRawGroupIdWithDefaultString(), dtoV1.getGroupId());
        assertEquals(gavV1.getRawArtifactId(), dtoV1.getId());
        assertEquals(gavV1.getRawVersionId(), dtoV1.getVersion());

        var otherBranchId = new BranchId("other");
        storage().createOrUpdateArtifactBranch(gavV1, otherBranchId);

        content = ContentHandle.create(OPENAPI_CONTENT_V2);
        var dtoV2 = storage().updateArtifact(ga.getRawGroupIdWithDefaultString(), ga.getRawArtifactId(), null, ArtifactType.OPENAPI, content, null);
        assertNotNull(dtoV2);
        assertEquals(ga.getRawGroupIdWithDefaultString(), dtoV2.getGroupId());
        assertEquals(ga.getRawArtifactId(), dtoV2.getId());

        branches = storage().getArtifactBranches(ga);
        assertEquals(Map.of(
                BranchId.LATEST, List.of(new GAV(ga, dtoV2.getVersion()), new GAV(ga, dtoV1.getVersion())),
                otherBranchId, List.of(new GAV(ga, dtoV1.getVersion()))
        ), branches);

        latestBranch = storage().getArtifactBranch(ga, BranchId.LATEST, DEFAULT);
        assertEquals(List.of(new GAV(ga, dtoV2.getVersion()), new GAV(ga, dtoV1.getVersion())), latestBranch);

        var otherBranch = storage().getArtifactBranch(ga, otherBranchId, DEFAULT);
        assertEquals(List.of(new GAV(ga, dtoV1.getVersion())), otherBranch);

        var gavV2 = storage().getArtifactBranchLeaf(ga, BranchId.LATEST, DEFAULT);
        assertNotNull(gavV2);
        assertEquals(gavV2.getRawGroupIdWithDefaultString(), dtoV2.getGroupId());
        assertEquals(gavV2.getRawArtifactId(), dtoV2.getId());
        assertEquals(gavV2.getRawVersionId(), dtoV2.getVersion());

        gavV1 = storage().getArtifactBranchLeaf(ga, otherBranchId, DEFAULT);
        assertNotNull(gavV1);
        assertEquals(gavV1.getRawGroupIdWithDefaultString(), dtoV1.getGroupId());
        assertEquals(gavV1.getRawArtifactId(), dtoV1.getId());
        assertEquals(gavV1.getRawVersionId(), dtoV1.getVersion());

        storage().createOrUpdateArtifactBranch(gavV2, otherBranchId);

        branches = storage().getArtifactBranches(ga);
        assertEquals(Map.of(
                BranchId.LATEST, List.of(new GAV(ga, dtoV2.getVersion()), new GAV(ga, dtoV1.getVersion())),
                otherBranchId, List.of(new GAV(ga, dtoV2.getVersion()), new GAV(ga, dtoV1.getVersion()))
        ), branches);

        assertEquals(storage().getArtifactBranch(ga, BranchId.LATEST, DEFAULT), storage().getArtifactBranch(ga, otherBranchId, DEFAULT));
        assertEquals(storage().getArtifactBranchLeaf(ga, BranchId.LATEST, DEFAULT), storage().getArtifactBranchLeaf(ga, otherBranchId, DEFAULT));

        storage().updateArtifactState(gavV2.getRawGroupIdWithDefaultString(), gavV2.getRawArtifactId(), gavV2.getRawVersionId(), ArtifactState.DISABLED);
        assertEquals(List.of(gavV1), storage().getArtifactBranch(ga, BranchId.LATEST, SKIP_DISABLED_LATEST));
        assertEquals(gavV1, storage().getArtifactBranchLeaf(ga, BranchId.LATEST, ArtifactRetrievalBehavior.SKIP_DISABLED_LATEST));

        storage().updateArtifactState(gavV2.getRawGroupIdWithDefaultString(), gavV2.getRawArtifactId(), gavV2.getRawVersionId(), ArtifactState.ENABLED);
        assertEquals(List.of(gavV2, gavV1), storage().getArtifactBranch(ga, BranchId.LATEST, SKIP_DISABLED_LATEST));
        assertEquals(gavV2, storage().getArtifactBranchLeaf(ga, BranchId.LATEST, ArtifactRetrievalBehavior.SKIP_DISABLED_LATEST));

        storage().deleteArtifactVersion(gavV1.getRawGroupIdWithDefaultString(), gavV1.getRawArtifactId(), gavV1.getRawVersionId());

        assertEquals(List.of(gavV2), storage().getArtifactBranch(ga, BranchId.LATEST, DEFAULT));
        assertEquals(List.of(gavV2), storage().getArtifactBranch(ga, otherBranchId, DEFAULT));

        storage().deleteArtifactBranch(ga, otherBranchId);

        assertThrows(BranchNotFoundException.class, () -> storage().getArtifactBranch(ga, otherBranchId, DEFAULT));
        assertThrows(VersionNotFoundException.class, () -> storage().getArtifactBranchLeaf(ga, otherBranchId, DEFAULT));

        assertThrows(NotAllowedException.class, () -> storage().deleteArtifactBranch(ga, BranchId.LATEST));
    }


    private static String generateString(int size) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            builder.append("a");
        }
        assertEquals(size, builder.toString().length());
        return builder.toString();
    }

}
