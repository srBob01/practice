package backend.academy.scrapper.service.base.impl.it.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.repository.jdbc.TagJdbcRepository;
import backend.academy.scrapper.service.base.impl.jdbc.TagServiceJdbcImpl;
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
@Import({TagJdbcRepository.class, TagServiceJdbcImpl.class})
@Sql(scripts = "classpath:db/changelog/changeset/004-create-tag-table.sql")
@TestPropertySource(properties = {"spring.liquibase.enabled=false", "access-type=SQL"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("TagServiceJdbcImpl — интеграционные тесты на Postgres‑контейнере")
class TagServiceJdbcImplTestIT {

    @SuppressWarnings("resource")
    @SuppressFBWarnings(
            value = "RV_RESOURCE_LEAK",
            justification = "PostgreSQLContainer lifecycle managed by Testcontainers")
    @ServiceConnection
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private TagServiceJdbcImpl service;

    @Test
    @DisplayName("getOrCreateTagId: создаёт новый и возвращает ID")
    void getOrCreate_createsNew() {
        // Arrange
        String name = "foo";

        // Act
        Long id = service.getOrCreateTagId(name);

        // Assert
        assertThat(id).isGreaterThan(0);

        // Arrange (DB проверка)
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
    @DisplayName("getOrCreateTagId: повторный вызов возвращает тот же ID")
    void getOrCreate_returnsExisting() {
        // Arrange
        String name = "dup";
        Long first = service.getOrCreateTagId(name);

        // Act
        Long second = service.getOrCreateTagId(name);

        // Assert
        assertThat(second).isEqualTo(first);

        // Arrange (DB проверка)
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
    @DisplayName("getTagIdByName: на существующем теге возвращает Optional с ID")
    void getByName_found() {
        // Arrange
        String name = "bar";
        Long id = service.getOrCreateTagId(name);

        // Act
        Optional<Long> opt = service.getTagIdByName(name);

        // Assert
        assertThat(opt).hasValue(id);
    }

    @Test
    @DisplayName("getTagIdByName: на несуществующем теге возвращает пустой Optional")
    void getByName_missing() {
        // Arrange
        String name = "no_such";

        // Act
        Optional<Long> opt = service.getTagIdByName(name);

        // Assert
        assertThat(opt).isEmpty();
    }
}
