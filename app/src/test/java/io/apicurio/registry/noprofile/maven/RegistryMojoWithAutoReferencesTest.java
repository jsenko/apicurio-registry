package io.apicurio.registry.noprofile.maven;

import io.apicurio.registry.maven.DownloadRegistryMojo;
import io.apicurio.registry.maven.RegisterArtifact;
import io.apicurio.registry.maven.RegisterRegistryMojo;
import io.apicurio.registry.rest.client.models.ArtifactReference;
import io.apicurio.registry.rest.client.models.HandleReferencesType;
import io.apicurio.registry.rest.client.models.VersionMetaData;
import io.apicurio.registry.rest.v3.beans.IfArtifactExists;
import io.apicurio.registry.types.ArtifactType;
import io.apicurio.registry.utils.IoUtil;
import io.apicurio.registry.utils.tests.TestUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@QuarkusTest
public class RegistryMojoWithAutoReferencesTest extends RegistryMojoTestBase {

    private static final String PROTO_SCHEMA_EXTENSION = ".proto";
    private static final String AVSC_SCHEMA_EXTENSION = ".avsc";
    private static final String JSON_SCHEMA_EXTENSION = ".json";

    RegisterRegistryMojo registerMojo;
    DownloadRegistryMojo downloadMojo;

    @BeforeEach
    public void createMojos() {
        this.registerMojo = new RegisterRegistryMojo();
        this.registerMojo.setRegistryUrl(TestUtils.getRegistryV3ApiUrl(testPort));

        this.downloadMojo = new DownloadRegistryMojo();
        this.downloadMojo.setRegistryUrl(TestUtils.getRegistryV3ApiUrl(testPort));
    }

    @Test
    public void autoRegisterAvroWithReferences() throws Exception {
        String groupId = "autoRegisterAvroWithReferences";
        String artifactId = "tradeRaw";

        File tradeRawFile = new File(getClass().getResource("TradeRawArray.avsc").getFile());

        Set<String> avroFiles = Arrays.stream(Objects.requireNonNull(
                tradeRawFile.getParentFile().listFiles((dir, name) -> name.endsWith(AVSC_SCHEMA_EXTENSION))))
                .map(file -> {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                    }
                    return IoUtil.toString(fis);
                }).collect(Collectors.toSet());

        RegisterArtifact tradeRawArtifact = new RegisterArtifact();
        tradeRawArtifact.setGroupId(groupId);
        tradeRawArtifact.setArtifactId(artifactId);
        tradeRawArtifact.setArtifactType(ArtifactType.AVRO);
        tradeRawArtifact.setFile(tradeRawFile);
        tradeRawArtifact.setAnalyzeDirectory(true);
        tradeRawArtifact.setIfExists(IfArtifactExists.FAIL);

        registerMojo.setArtifacts(Collections.singletonList(tradeRawArtifact));
        registerMojo.execute();

        // Assertions
        validateStructure(groupId, artifactId, 1, 3, avroFiles);
    }

    @Test
    public void autoRegisterProtoWithReferences() throws Exception {
        // Preparation
        String groupId = "autoRegisterProtoWithReferences";
        String artifactId = "tableNotification";

        File tableNotificationFile = new File(getClass().getResource("table_notification.proto").getFile());

        Set<String> protoFiles = Arrays.stream(Objects.requireNonNull(tableNotificationFile.getParentFile()
                .listFiles((dir, name) -> name.endsWith(PROTO_SCHEMA_EXTENSION)))).map(file -> {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                    }
                    return IoUtil.toString(fis).trim();
                }).collect(Collectors.toSet());

        RegisterArtifact tableNotification = new RegisterArtifact();
        tableNotification.setGroupId(groupId);
        tableNotification.setArtifactId(artifactId);
        tableNotification.setArtifactType(ArtifactType.PROTOBUF);
        tableNotification.setFile(tableNotificationFile);
        tableNotification.setAnalyzeDirectory(true);
        tableNotification.setIfExists(IfArtifactExists.FAIL);

        registerMojo.setArtifacts(Collections.singletonList(tableNotification));

        // Execution
        registerMojo.execute();

        // Assertions
        validateStructure(groupId, artifactId, 2, 4, protoFiles);
    }

    @Test
    public void autoRegisterJsonSchemaWithReferences() throws Exception {
        // Preparation
        String groupId = "autoRegisterJsonSchemaWithReferences";
        String artifactId = "citizen";

        File citizenFile = new File(getClass().getResource("citizen.json").getFile());

        Set<String> protoFiles = Arrays.stream(Objects.requireNonNull(
                citizenFile.getParentFile().listFiles((dir, name) -> name.endsWith(JSON_SCHEMA_EXTENSION))))
                .map(file -> {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                    }
                    return IoUtil.toString(fis).trim();
                }).collect(Collectors.toSet());

        RegisterArtifact citizen = new RegisterArtifact();
        citizen.setGroupId(groupId);
        citizen.setArtifactId(artifactId);
        citizen.setArtifactType(ArtifactType.JSON);
        citizen.setFile(citizenFile);
        citizen.setAnalyzeDirectory(true);
        citizen.setIfExists(IfArtifactExists.FAIL);

        registerMojo.setArtifacts(Collections.singletonList(citizen));

        // Execution
        registerMojo.execute();

        // Assertions
        validateStructure(groupId, artifactId, 3, 4, protoFiles);
    }

    @Test
    public void autoRegisterJsonSchemaWithReferencesDeref() throws Exception {
        // Preparation
        String groupId = "autoRegisterJsonSchemaWithReferencesDeref";
        String artifactId = "stock";
        String version = "1.0.0";

        File stockFile = new File(getClass().getResource("./stock/FLIStockAdjustment.json").getFile());

        Set<String> jsonFiles = Arrays.stream(Objects.requireNonNull(
                stockFile.getParentFile().listFiles((dir, name) -> name.endsWith(JSON_SCHEMA_EXTENSION))))
                .map(file -> {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                    }
                    return IoUtil.toString(fis).trim();
                }).collect(Collectors.toSet());

        RegisterArtifact stock = new RegisterArtifact();
        stock.setAutoRefs(true);
        stock.setGroupId(groupId);
        stock.setArtifactId(artifactId);
        stock.setVersion(version);
        stock.setArtifactType(ArtifactType.JSON);
        stock.setFile(stockFile);
        stock.setIfExists(IfArtifactExists.FIND_OR_CREATE_VERSION);

        registerMojo.setArtifacts(Collections.singletonList(stock));

        // Execution
        registerMojo.execute();

        // Assertions
        validateStructure(groupId, artifactId, 9, 6, jsonFiles);

        final VersionMetaData artifactWithReferences = clientV3.groups().byGroupId(groupId).artifacts()
                .byArtifactId(artifactId).versions().byVersionExpression(version).get();
        InputStream contentByGlobalId = clientV3.ids().globalIds()
                .byGlobalId(artifactWithReferences.getGlobalId()).get(getRequestConfiguration -> {
                    getRequestConfiguration.queryParameters.references = HandleReferencesType.DEREFERENCE;
                });

        File stockFileDeref = new File(
                getClass().getResource("./stock/FLIStockAdjustment_deref.json").getFile());

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(stockFileDeref);
        } catch (FileNotFoundException e) {
        }

        Assertions.assertEquals(IoUtil.toString(fis).trim(), IoUtil.toString(contentByGlobalId));
    }

    private void validateStructure(String groupId, String artifactId, int expectedMainReferences,
            int expectedTotalArtifacts, Set<String> originalContents) throws Exception {
        final VersionMetaData artifactWithReferences = clientV3.groups().byGroupId(groupId).artifacts()
                .byArtifactId(artifactId).versions().byVersionExpression("branch=latest").get();
        final String mainContent = new String(clientV3.groups().byGroupId(groupId).artifacts()
                .byArtifactId(artifactId).versions().byVersionExpression(artifactWithReferences.getVersion())
                .content().get().readAllBytes(), StandardCharsets.UTF_8);

        Assertions.assertTrue(originalContents.contains(mainContent)); // The main content has been registered
        // as-is.

        final List<ArtifactReference> mainArtifactReferences = clientV3.ids().globalIds()
                .byGlobalId(artifactWithReferences.getGlobalId()).references().get();

        // The main artifact has the expected number of references
        Assertions.assertEquals(expectedMainReferences, mainArtifactReferences.size());

        // Validate all the contents are registered as they are in the file system.
        validateReferences(mainArtifactReferences, originalContents);

        // The total number of artifacts for the directory structure is the expected.
        Assertions.assertEquals(expectedTotalArtifacts,
                clientV3.groups().byGroupId(groupId).artifacts().get().getCount().intValue());
    }

    private void validateReferences(List<ArtifactReference> artifactReferences, Set<String> loadedContents)
            throws Exception {
        for (ArtifactReference artifactReference : artifactReferences) {
            String referenceContent = new String(clientV3.groups().byGroupId(artifactReference.getGroupId())
                    .artifacts().byArtifactId(artifactReference.getArtifactId()).versions()
                    .byVersionExpression(artifactReference.getVersion()).content().get().readAllBytes(),
                    StandardCharsets.UTF_8);
            VersionMetaData referenceMetadata = clientV3.groups().byGroupId(artifactReference.getGroupId())
                    .artifacts().byArtifactId(artifactReference.getArtifactId()).versions()
                    .byVersionExpression("branch=latest").get();
            Assertions.assertTrue(loadedContents.contains(referenceContent.trim()));

            List<ArtifactReference> nestedReferences = clientV3.ids().globalIds()
                    .byGlobalId(referenceMetadata.getGlobalId()).references().get();

            if (!nestedReferences.isEmpty()) {
                validateReferences(nestedReferences, loadedContents);
            }
        }
    }
}