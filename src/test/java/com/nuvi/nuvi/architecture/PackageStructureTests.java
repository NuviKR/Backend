package com.nuvi.nuvi.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PackageStructureTests {

    private static final Path MAIN_JAVA = Path.of("src", "main", "java");

    @Test
    void authInfraClassesAreGroupedByRoleSpecificSubpackage() throws IOException {
        List<Path> flatInfraFiles = javaFiles()
                .filter(path -> normalized(path).contains("/com/nuvi/nuvi/auth/infra/"))
                .filter(path -> MAIN_JAVA.relativize(path).getNameCount() == 6)
                .toList();

        assertThat(flatInfraFiles).isEmpty();
    }

    @Test
    void authJpaEntitiesStayUnderInfraEntity() throws IOException {
        List<Path> misplacedEntities = javaFiles()
                .filter(path -> normalized(path).contains("/com/nuvi/nuvi/auth/"))
                .filter(path -> read(path).contains("@Entity"))
                .filter(path -> !normalized(path).contains("/auth/infra/entity/"))
                .toList();

        assertThat(misplacedEntities).isEmpty();
    }

    @Test
    void authSpringDataRepositoriesStayUnderInfraRepository() throws IOException {
        List<Path> misplacedSpringDataRepositories = javaFiles()
                .filter(path -> normalized(path).contains("/com/nuvi/nuvi/auth/"))
                .filter(path -> read(path).contains("extends JpaRepository"))
                .filter(path -> !normalized(path).contains("/auth/infra/repository/"))
                .toList();

        assertThat(misplacedSpringDataRepositories).isEmpty();
    }

    @Test
    void authDomainRepositoryPortsStayUnderDomainRepository() throws IOException {
        List<Path> misplacedDomainRepositories = javaFiles()
                .filter(path -> normalized(path).contains("/com/nuvi/nuvi/auth/domain/"))
                .filter(path -> path.getFileName().toString().endsWith("Repository.java"))
                .filter(path -> !normalized(path).contains("/auth/domain/repository/"))
                .toList();

        assertThat(misplacedDomainRepositories).isEmpty();
    }

    @Test
    void apiDtoContainersStayUnderControllerDtoPackages() throws IOException {
        List<Path> misplacedDtoContainers = javaFiles()
                .filter(path -> normalized(path).matches(".*/com/nuvi/nuvi/(auth|cart|onboarding)/.*Dtos\\.java"))
                .filter(path -> !normalized(path).contains("/controller/dto/"))
                .toList();

        assertThat(misplacedDtoContainers).isEmpty();
    }

    @Test
    void authJpaEntitiesAreNotExposedThroughControllerOrApplicationPackages() throws IOException {
        List<Path> entityLeakingEntrypoints = javaFiles()
                .filter(path -> normalized(path).matches(".*/com/nuvi/nuvi/auth/(controller|application)/.*\\.java"))
                .filter(path -> read(path).contains(".infra.entity."))
                .toList();

        assertThat(entityLeakingEntrypoints).isEmpty();
    }

    private static java.util.stream.Stream<Path> javaFiles() throws IOException {
        return Files.walk(MAIN_JAVA)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".java"));
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path, exception);
        }
    }

    private static String normalized(Path path) {
        return path.toString().replace('\\', '/');
    }
}
