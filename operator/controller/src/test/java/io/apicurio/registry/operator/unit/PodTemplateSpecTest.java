package io.apicurio.registry.operator.unit;

import io.apicurio.registry.operator.api.v1.ApicurioRegistry3;
import io.apicurio.registry.operator.api.v1.ApicurioRegistry3Spec;
import io.apicurio.registry.operator.api.v1.spec.AppSpec;
import io.apicurio.registry.operator.api.v1.spec.UiSpec;
import io.apicurio.registry.operator.resource.ResourceFactory;
import io.apicurio.registry.operator.status.StatusManager;
import io.apicurio.registry.operator.status.ValidationErrorConditionManager;
import io.apicurio.registry.operator.unit.PodTemplateSpecArgumentProviders.*;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class PodTemplateSpecTest {

    private static final Logger log = LoggerFactory.getLogger(PodTemplateSpecTest.class);

    @ParameterizedTest
    @ArgumentsSource(AppPositiveTestCases.class)
    void testAppPositive(TestCase testCase) {
        log.info("Running test case: {}", testCase.getId());
        var primary = getPrimary();
        primary.getSpec().getApp().setPodTemplateSpec(testCase.getSpec());
        var expected = ResourceFactory.INSTANCE.getDefaultAppDeployment(primary).getSpec().getTemplate();
        preprocessTestCaseExpected(testCase.getExpected());
        assertThat(expected)
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("spec.containers", "spec.containers.ports")
                .isEqualTo(testCase.getExpected());
        assertThat(testCase.getExpected())
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("spec.containers", "spec.containers.ports")
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @ArgumentsSource(AppNegativeTestCases.class)
    void testAppNegative(TestCase testCase) {
        log.info("Running test case: {}", testCase.getId());
        var primary = getPrimary();
        try {
            primary.getSpec().getApp().setPodTemplateSpec(testCase.getSpec());
            ResourceFactory.INSTANCE.getDefaultAppDeployment(primary);
            assertThat(StatusManager.get(primary).getConditionManager(ValidationErrorConditionManager.class).hasErrors()).isTrue();
        } finally {
            StatusManager.clean(primary);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(UIPositiveTestCases.class)
    void testUIPositive(TestCase testCase) {
        log.info("Running test case: {}", testCase.getId());
        var primary = getPrimary();
        primary.getSpec().getUi().setPodTemplateSpec(testCase.getSpec());
        var expected = ResourceFactory.INSTANCE.getDefaultUIDeployment(primary).getSpec().getTemplate();
        preprocessTestCaseExpected(testCase.getExpected());
        assertThat(expected)
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("spec.containers", "spec.containers.ports")
                .isEqualTo(testCase.getExpected());
        assertThat(testCase.getExpected())
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("spec.containers", "spec.containers.ports")
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @ArgumentsSource(UINegativeTestCases.class)
    void testUINegative(TestCase testCase) {
        log.info("Running test case: {}", testCase.getId());
        var primary = getPrimary();
        try {
            primary.getSpec().getUi().setPodTemplateSpec(testCase.getSpec());
            ResourceFactory.INSTANCE.getDefaultUIDeployment(primary);
            assertThat(StatusManager.get(primary).getConditionManager(ValidationErrorConditionManager.class).hasErrors()).isTrue();
        } finally {
            StatusManager.clean(primary);
        }
    }

    private static ApicurioRegistry3 getPrimary() {
        var primary = new ApicurioRegistry3();
        primary.setMetadata(new ObjectMeta());
        primary.getMetadata().setName("test");
        primary.getMetadata().setNamespace("test");
        primary.setSpec(new ApicurioRegistry3Spec());
        primary.getSpec().setApp(new AppSpec());
        primary.getSpec().setUi(new UiSpec());
        return primary;
    }

    private static void preprocessTestCaseExpected(PodTemplateSpec expected) {
        expected.getMetadata().getLabels().computeIfPresent("app.kubernetes.io/version",
                (k, v) -> ConfigProvider.getConfig().getValue("registry.version", String.class));
    }
}
