package backend.academy.scrapper.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TagJdbcRepository.class)
@Sql(scripts = "classpath:db/changelog/changeset/004-create-tag-table.sql")
@TestPropertySource(properties = {"spring.liquibase.enabled=false"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("TagJdbcRepository — интеграционные тесты на Postgres‑контейнере")
class TagJdbcRepositoryTestIT {

    @SuppressWarnings("resource")
    @SuppressFBWarnings(
            value = "RV_RESOURCE_LEAK",
            justification = "PostgreSQLContainer lifecycle managed by Testcontainers JUnit extension")
    @ServiceConnection
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    JdbcClient jdbcClient;

    @Autowired
    TagJdbcRepository repo;

    @Test
    @DisplayName("insertOrGetIdByName: создаёт новый тег и возвращает его ID")
    void insertOrGet_createsNew() {
        // Arrange
        String name = "foo";

        // Act
        long id = repo.insertOrGetIdByName(name);

        // Assert
        assertThat(id).isGreaterThan(0);

        // Arrange (DB check)
        // Act
        Long count = jdbcClient
                .sql("SELECT count(*) FROM tag WHERE name = :name")
                .param("name", name)
                .query(Long.class)
                .single();
        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("insertOrGetIdByName: при дубликате возвращает существующий ID")
    void insertOrGet_returnsExisting() {
        // Arrange
        String name = "dup";
        long first = repo.insertOrGetIdByName(name);

        // Act
        long second = repo.insertOrGetIdByName(name);

        // Assert
        assertThat(second).isEqualTo(first);

        // Arrange (DB check)
        // Act
        Long cnt = jdbcClient
                .sql("SELECT count(*) FROM tag WHERE name = :n")
                .param("n", name)
                .query(Long.class)
                .single();
        // Assert
        assertThat(cnt).isEqualTo(1);
    }

    @Test
    @DisplayName("getIdByName: возвращает Optional с ID, если тег есть")
    void getIdByName_found() {
        // Arrange
        String name = "bar";
        long id = repo.insertOrGetIdByName(name);

        // Act
        Optional<Long> opt = repo.getIdByName(name);

        // Assert
        assertThat(opt).hasValue(id);
    }

    @Test
    @DisplayName("getIdByName: возвращает пустой Optional, если тега нет")
    void getIdByName_missing() {
        // Arrange
        String name = "does_not_exist";

        // Act
        Optional<Long> opt = repo.getIdByName(name);

        // Assert
        assertThat(opt).isEmpty();
    }
}
