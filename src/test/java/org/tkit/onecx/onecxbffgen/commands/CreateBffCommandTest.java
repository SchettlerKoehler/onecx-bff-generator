package org.tkit.onecx.onecxbffgen.commands;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
class CreateBffCommandTest {
    @Test
    void shouldGenerateProjectWithModernProfile() throws Exception {
        Path tempDir = Files.createTempDirectory("bff-generator-modern-");
        Path frontend = Path.of("src/test/resources/openapi/frontend.yaml").toAbsolutePath();
        Path backend = Path.of("src/test/resources/openapi/backend.yaml").toAbsolutePath();
        int result = new CommandLine(new CreateBffCommand(new org.tkit.onecx.onecxbffgen.service.GeneratorService())).execute(
                "--project-name", "orders-bff",
                "--frontend-api", frontend.toString(),
                "--backend-api", backend.toString(),
                "--output-dir", tempDir.toString()
        );
        Path generated = tempDir.resolve("orders-bff");
        assertEquals(0, result);
        assertTrue(Files.exists(generated.resolve("pom.xml")));
        assertTrue(Files.exists(generated.resolve("README.md")));
        assertTrue(Files.exists(generated.resolve("src/main/openapi/frontend.yaml")));
        assertTrue(Files.exists(generated.resolve("openapi/clients/backend.yaml")), "Local backend spec should be copied into openapi/clients/");
        assertTrue(Files.readString(generated.resolve("README.md"))
                .contains("copied to `openapi/clients/<original-file-name>`"));
        assertFalse(Files.exists(generated.resolve("src/main/openapi/openapi-frontend.yaml")));
        assertFalse(Files.exists(generated.resolve("target/tmp/openapi/openapi-backend.yaml")));
        assertTrue(Files.exists(generated.resolve("src/main/java/org/tkit/onecx/orders/bff/rs/controllers/UsersRestController.java")));
        assertTrue(Files.exists(generated.resolve("src/main/java/org/tkit/onecx/orders/bff/rs/mappers/UserMapper.java")));
        assertTrue(Files.exists(generated.resolve("src/main/java/org/tkit/onecx/orders/bff/rs/mappers/ExceptionMapper.java")));
        assertTrue(Files.exists(generated.resolve("src/test/java/org/tkit/onecx/orders/bff/rs/UsersRestControllerTest.java")));
        assertTrue(Files.exists(generated.resolve("src/test/java/org/tkit/onecx/orders/bff/rs/UsersRestControllerIT.java")));
        assertTrue(Files.exists(generated.resolve("src/test/resources/mockserver.properties")));
        assertTrue(Files.exists(generated.resolve("src/main/helm/Chart.yaml")));
        assertTrue(Files.exists(generated.resolve("src/main/helm/values.yaml")));
        assertTrue(Files.exists(generated.resolve("src/main/docker/Dockerfile.jvm")));
        assertTrue(Files.exists(generated.resolve("src/main/docker/Dockerfile.native")));
        String dockerJvm = Files.readString(generated.resolve("src/main/docker/Dockerfile.jvm"));
        assertTrue(dockerJvm.startsWith("FROM ghcr.io/onecx/docker-quarkus-jvm:"), "Dockerfile.jvm should use docker-quarkus-jvm image");
        assertFalse(dockerJvm.contains("${dockerJvmVersion}"), "Dockerfile.jvm placeholder must be resolved");
        String dockerNative = Files.readString(generated.resolve("src/main/docker/Dockerfile.native"));
        assertTrue(dockerNative.startsWith("FROM ghcr.io/onecx/docker-quarkus-native:"), "Dockerfile.native should use docker-quarkus-native image");
        assertFalse(dockerNative.contains("${dockerNativeVersion}"), "Dockerfile.native placeholder must be resolved");
        String chart = Files.readString(generated.resolve("src/main/helm/Chart.yaml"));
        assertTrue(chart.contains("name: helm-quarkus-app"), "Chart.yaml should reference helm-quarkus-app");
        assertTrue(chart.contains("name: orders-bff"), "Chart metadata should use the complete project artifact identity");
        assertFalse(chart.contains("${helmVersion}"), "Chart.yaml placeholder must be resolved");
        String helmValues = Files.readString(generated.resolve("src/main/helm/values.yaml"));
        assertTrue(helmValues.contains("repository: \"onecx/orders-bff\""));
        assertTrue(Files.exists(generated.resolve(".github/workflows/build-branch.yml")));
        assertTrue(Files.exists(generated.resolve(".github/workflows/build.yml")));
        assertTrue(Files.exists(generated.resolve(".github/workflows/build-pr.yml")));
        assertTrue(Files.exists(generated.resolve(".github/workflows/build-pr-merge.yml")));
        assertTrue(Files.exists(generated.resolve(".github/workflows/build-release.yml")));
        assertTrue(Files.exists(generated.resolve(".github/workflows/create-fix-branch.yml")));
        assertTrue(Files.exists(generated.resolve(".github/workflows/create-new-build.yml")));
        assertTrue(Files.exists(generated.resolve(".github/workflows/create-release.yml")));
        assertTrue(Files.exists(generated.resolve(".github/workflows/documentation.yml")));
        assertTrue(Files.exists(generated.resolve(".github/workflows/security.yml")));
        assertTrue(Files.exists(generated.resolve(".github/workflows/sonar-pr.yml")));
        assertTrue(Files.exists(generated.resolve(".github/renovate.json")));
        String renovate = Files.readString(generated.resolve(".github/renovate.json"));
        assertTrue(renovate.contains("renovate-schema.json"), ".github/renovate.json should contain schema reference");
        assertTrue(renovate.contains("onecx/onecx-renovate"), ".github/renovate.json should extend onecx renovate config");
        String pom = Files.readString(generated.resolve("pom.xml"));
        assertTrue(pom.contains("<groupId>org.tkit.onecx</groupId>"));
        assertTrue(pom.contains("<artifactId>orders-bff</artifactId>"));
        assertTrue(pom.contains("<packaging>quarkus</packaging>"));
        assertFalse(pom.contains("<maven.compiler.release>"));
        assertTrue(pom.contains("<artifactId>quarkus-openapi-generator</artifactId>"));
        assertTrue(pom.contains("<artifactId>tkit-quarkus-rest-context</artifactId>"));
        assertTrue(pom.contains("<artifactId>onecx-permissions</artifactId>"));
        assertTrue(pom.contains("<artifactId>quarkus-junit</artifactId>"), "pom.xml should contain quarkus-junit test dependency");
        assertTrue(pom.contains("<artifactId>quarkus-junit-mockito</artifactId>"), "pom.xml should contain quarkus-junit-mockito test dependency");
        assertFalse(pom.contains("<artifactId>maven-surefire-plugin</artifactId>"));
        assertFalse(pom.contains("SecurityDynamicImplTest"));
        assertFalse(pom.contains("<artifactId>swagger-parser</artifactId>"));
        assertFalse(pom.contains("<artifactId>tkit-quarkus-security</artifactId>"));
        assertTrue(pom.contains("<name>Orders Backend For Frontend</name>"));
        assertFalse(pom.contains("download-maven-plugin"), "pom.xml should NOT contain download plugin when backend is a local file");
        assertFalse(pom.contains("download-backend-api"), "pom.xml should NOT contain download-backend-api when backend is a local file");
        // Local backend file is copied into project, not referenced in pom.xml download plugin
        String usersController = Files.readString(generated.resolve("src/main/java/org/tkit/onecx/orders/bff/rs/controllers/UsersRestController.java"));
        assertTrue(usersController.contains("UserDTO"), "Frontend DTO type should be used in controller method signature");
        assertFalse(usersController.contains("UserDTODTO"), "DTO suffix should not be doubled");
        String usersTest = Files.readString(generated.resolve("src/test/java/org/tkit/onecx/orders/bff/rs/UsersRestControllerTest.java"));
        assertTrue(usersTest.contains("@QuarkusTest"), "Test should be a QuarkusTest");
        assertTrue(usersTest.contains("keycloakClient.getAccessToken"), "Test should use Keycloak auth");
        assertTrue(usersTest.contains("MockServerClient"), "Test should use MockServerClient");
        assertTrue(usersTest.contains("UNAUTHORIZED"), "Test should verify 401");
        String abstractTest = Files.readString(generated.resolve("src/test/java/org/tkit/onecx/orders/bff/rs/AbstractTest.java"));
        assertTrue(abstractTest.contains("KeycloakTestClient"), "AbstractTest should have KeycloakTestClient");
        assertTrue(abstractTest.contains("MockServerTestResource"), "AbstractTest should register MockServerTestResource");
        // @InjectMockServerClient is declared in each test class (not abstract base)
        assertTrue(usersTest.contains("@InjectMockServerClient"), "Test class should inject MockServerClient");
        String appProps = Files.readString(generated.resolve("src/main/resources/application.properties"));
        assertTrue(appProps.contains("%prod.quarkus.rest-client.backend_api.url=http://orders-svc:8080"));
        assertTrue(appProps.contains("quarkus.openapi-generator.codegen.spec.backend_yaml.base-package=gen.org.tkit.onecx.orders.bff.client"));
        String report = Files.readString(generated.resolve("generation-report.json"));
        assertTrue(report.contains("\"artifactId\": \"orders-bff\""));
        assertTrue(report.contains("\"backendServiceName\": \"orders-svc\""));
    }
    @Test
    void shouldAvoidDuplicatedOnecxPackageSegmentForOnecxPrefixedArtifact() throws Exception {
        Path tempDir = Files.createTempDirectory("bff-generator-package-");
        Path frontend = Path.of("src/test/resources/openapi/frontend.yaml").toAbsolutePath();
        Path backend = Path.of("src/test/resources/openapi/backend.yaml").toAbsolutePath();
        int result = new CommandLine(new CreateBffCommand(new org.tkit.onecx.onecxbffgen.service.GeneratorService())).execute(
                "--project-name", "onecx-orders-bff",
                "--group-id", "org.tkit.onecx",
                "--frontend-api", frontend.toString(),
                "--backend-api", backend.toString(),
                "--output-dir", tempDir.toString()
        );
        Path generated = tempDir.resolve("onecx-orders-bff");
        assertEquals(0, result);
        assertTrue(Files.exists(generated.resolve("src/main/java/org/tkit/onecx/orders/bff/rs/controllers/UsersRestController.java")));
        assertFalse(Files.exists(generated.resolve("src/main/java/org/tkit/onecx/onecx/orders/bff/rs/controllers/UsersRestController.java")));
        String appProps = Files.readString(generated.resolve("src/main/resources/application.properties"));
        assertTrue(appProps.contains("%prod.quarkus.rest-client.backend_api.url=http://onecx-orders-svc:8080"));
    }
    @Test
    void shouldGenerateInPlaceWhenOutputDirAlreadyPointsToArtifactFolder() throws Exception {
        Path tempDir = Files.createTempDirectory("bff-generator-in-place-");
        Path projectDir = tempDir.resolve("onecx-demo-bff");
        Files.createDirectories(projectDir);
        Path frontend = Path.of("src/test/resources/openapi/frontend.yaml").toAbsolutePath();
        Path backend = Path.of("src/test/resources/openapi/backend.yaml").toAbsolutePath();
        int result = new CommandLine(new CreateBffCommand(new org.tkit.onecx.onecxbffgen.service.GeneratorService())).execute(
                "--project-name", "onecx-demo-bff",
                "--frontend-api", frontend.toString(),
                "--backend-api", backend.toString(),
                "--output-dir", projectDir.toString()
        );
        assertEquals(0, result);
        assertTrue(Files.exists(projectDir.resolve("pom.xml")));
        assertFalse(Files.exists(projectDir.resolve("onecx-demo-bff/pom.xml")));
    }
    @Test
    void shouldUseNameGroupAndPackageInputsInGeneratedConfig() throws Exception {
        Path tempDir = Files.createTempDirectory("bff-generator-name-group-package-");
        Path frontend = Path.of("src/test/resources/openapi/frontend.yaml").toAbsolutePath();
        Path backend = Path.of("src/test/resources/openapi/backend.yaml").toAbsolutePath();
        int result = new CommandLine(new CreateBffCommand(new org.tkit.onecx.onecxbffgen.service.GeneratorService())).execute(
                "--name", "acme-orders-bff",
                "--artifact-id", "acme-orders-bff",
                "--group", "com.acme",
                "--package", "com.customer.orders",
                "--frontend-api", frontend.toString(),
                "--backend-api", backend.toString(),
                "--output-dir", tempDir.toString()
        );
        Path generated = tempDir.resolve("acme-orders-bff");
        assertEquals(0, result);
        String pom = Files.readString(generated.resolve("pom.xml"));
        assertTrue(pom.contains("<groupId>com.acme</groupId>"));
        assertTrue(pom.contains("<artifactId>acme-orders-bff</artifactId>"));
        assertTrue(pom.contains("<name>Acme Orders Backend For Frontend</name>"));
        String appProps = Files.readString(generated.resolve("src/main/resources/application.properties"));
        assertTrue(appProps.contains("onecx.generator.name=acme-orders-bff"));
        assertTrue(appProps.contains("onecx.generator.group=com.acme"));
        assertTrue(appProps.contains("onecx.generator.package=com.customer.orders"));
        assertTrue(appProps.contains("onecx.permissions.application-id=${quarkus.application.name}"));
        assertTrue(appProps.contains("quarkus.openapi-generator.codegen.input-base-dir=openapi/clients"));
        assertTrue(appProps.contains(
                "%prod.quarkus.rest-client.backend_api.url=http://acme-orders-svc:8080"
        ));
        assertFalse(appProps.contains(
                "%prod.quarkus.rest-client.backend_api.url=http://localhost:8080"
        ));
        assertTrue(Files.exists(generated.resolve("src/main/java/com/customer/orders/rs/controllers/UsersRestController.java")));
        String report = Files.readString(generated.resolve("generation-report.json"));
        assertTrue(report.contains("\"artifactId\": \"acme-orders-bff\""));
        assertTrue(report.contains("\"backendServiceName\": \"acme-orders-svc\""));
    }

    @Test
    void shouldGenerateUnambiguousMapperWhenFrontendAndBackendTypesShareSimpleName() throws Exception {
        Path tempDir = Files.createTempDirectory("bff-generator-mapper-collision-");
        Path frontend = Path.of("src/test/resources/openapi/frontend-collision.yaml").toAbsolutePath();
        Path backend = Path.of("src/test/resources/openapi/backend-collision.yaml").toAbsolutePath();

        int result = new CommandLine(new CreateBffCommand(new org.tkit.onecx.onecxbffgen.service.GeneratorService())).execute(
                "--name", "onecx-demo-bff",
                "--group", "org.tkit.onecx",
                "--package", "org.tkit.onecx.demo",
                "--frontend-api", frontend.toString(),
                "--backend-api", backend.toString(),
                "--output-dir", tempDir.toString()
        );

        Path generated = tempDir.resolve("onecx-demo-bff");
        Path mapperPath = generated.resolve("src/main/java/org/tkit/onecx/demo/rs/mappers/CategoryMapper.java");
        assertEquals(0, result);
        assertTrue(Files.exists(mapperPath));

        String mapper = Files.readString(mapperPath);
        assertFalse(mapper.contains("import org.tkit.onecx.demo.bff.domain.model.frontend.Category;\nimport org.tkit.onecx.demo.bff.domain.model.backend.Category;"));
        assertTrue(mapper.contains("import gen.org.tkit.onecx.demo.rs.internal.model.CategoryDTO;"));
        assertTrue(mapper.contains("import gen.org.tkit.onecx.demo.client.model.Category;"));
        assertTrue(mapper.contains("Category toBackend(CategoryDTO source);"));
        assertTrue(mapper.contains("CategoryDTO toFrontend(Category source);"));
    }

    @Test
    void shouldGenerateFallbackControllerWithBackendModelImportsWhenFrontendHasNoPaths() throws Exception {
        Path tempDir = Files.createTempDirectory("bff-generator-fallback-");
        Path frontend = Path.of("src/test/resources/openapi/frontend-no-paths.yaml").toAbsolutePath();
        Path backend = Path.of("src/test/resources/openapi/backend-product.yaml").toAbsolutePath();
        
        int result = new CommandLine(new CreateBffCommand(new org.tkit.onecx.onecxbffgen.service.GeneratorService())).execute(
                "--project-name", "demo-fallback-bff",
                "--frontend-api", frontend.toString(),
                "--backend-api", backend.toString(),
                "--output-dir", tempDir.toString()
        );
        
        Path generated = tempDir.resolve("demo-fallback-bff");
        assertEquals(0, result);
        
        Path controllerPath = generated.resolve("src/main/java/org/tkit/onecx/demo/fallback/bff/rs/controllers/ProductRestController.java");
        assertTrue(Files.exists(controllerPath));
        
        String controller = Files.readString(controllerPath);
        // In fallback mode (frontend has no paths), backend model imports should be present
        assertTrue(controller.contains("import gen.org.tkit.onecx.demo.fallback.bff.client.model.*;"),
                "Controller should import backend model types for fallback mode");
        // Request/response types should use backend model names exactly as they appear in backend schema
        assertTrue(controller.contains("ProductSearchCriteriaDTO"),
                "Controller method parameter should use backend schema type");
    }

    @Test
    void shouldRejectAmbiguousProjectNameInsteadOfGuessingBackendServiceName() throws Exception {
        Path tempDir = Files.createTempDirectory("bff-generator-ambiguous-name-");
        Path frontend = Path.of("src/test/resources/openapi/frontend.yaml").toAbsolutePath();
        Path backend = Path.of("src/test/resources/openapi/backend.yaml").toAbsolutePath();

        int result = new CommandLine(new CreateBffCommand(new org.tkit.onecx.onecxbffgen.service.GeneratorService())).execute(
                "--name", "orders-bff-api",
                "--frontend-api", frontend.toString(),
                "--backend-api", backend.toString(),
                "--output-dir", tempDir.toString()
        );

        assertEquals(1, result);
        assertFalse(Files.exists(tempDir.resolve("orders-bff-api")));
    }
}
