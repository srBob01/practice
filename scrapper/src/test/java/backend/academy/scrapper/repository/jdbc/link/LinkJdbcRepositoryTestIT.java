package backend.academy.scrapper.repository.jdbc.link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.config.LinkBaseMapperConfig;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.repository.jdbc.mapper.LinkRowMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({LinkJdbcRepository.class, LinkRowMapper.class})
@Sql(scripts = {"classpath:db/changelog/changeset/002-create-link-table.sql"})
@SpringJUnitConfig(classes = LinkJdbcRepositoryTestIT.LinkServiceTestConfig.class)
@TestPropertySource(properties = "spring.liquibase.enabled=false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("LinkJdbcRepository — интеграционные тесты")
class LinkJdbcRepositoryTestIT {

    @TestConfiguration
    @Import(LinkBaseMapperConfig.class)
    @ComponentScan(basePackages = "backend.academy.scrapper.repository.jdbc.mapper.helper")
    static class LinkServiceTestConfig {}

    @SuppressWarnings("resource")
    @SuppressFBWarnings(
            value = "RV_RESOURCE_LEAK",
            justification = "PostgreSQLContainer lifecycle managed by Testcontainers JUnit extension")
    @ServiceConnection
    @Container
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    JdbcClient jdbc;

    @Autowired
    LinkJdbcRepository repo;

    @Nested
    @DisplayName("findIdByUrl()")
    class FindIdByUrl {
        @Test
        @DisplayName("пустая таблица → Optional.empty()")
        void whenEmpty_thenEmpty() {
            // Act & Assert
            assertThat(repo.findIdByUrl("nope")).isEmpty();
        }

        @Test
        @DisplayName("существующий URL → возвращает ID")
        void whenExists_thenReturnsId() {
            // Arrange
            Long id = jdbc.sql("INSERT INTO link (original_url, last_modified, last_checked, version, type) "
                            + "VALUES ('u', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB') RETURNING id")
                    .query(Long.class)
                    .single();

            // Act
            Optional<Long> found = repo.findIdByUrl("u");

            // Assert
            assertThat(found).hasValue(id);
        }
    }

    @Nested
    @DisplayName("insertLink()")
    class InsertLink {

        @Test
        @DisplayName("happy path: вставляет и присваивает ID (на примере GitHubLink)")
        void happyPath() {
            // Arrange
            var gh = new GitHubLink("http://x", "owner", "repo", "1", null);
            gh.lastModified(LocalDateTime.now());

            // Act
            repo.insertLink(gh);

            // Assert
            assertThat(gh.id()).isPositive();
            Long cnt = jdbc.sql("SELECT count(*) FROM link WHERE original_url = 'http://x'")
                    .query(Long.class)
                    .single();
            assertThat(cnt).isEqualTo(1);
        }

        @Test
        @DisplayName("duplicate → ScrapperException")
        void duplicateThrows() {
            // Arrange
            var gh = new GitHubLink("http://dup", "o", "r", "2", null);
            gh.lastModified(LocalDateTime.now());
            repo.insertLink(gh);

            // Act & Assert
            assertThatThrownBy(() -> repo.insertLink(gh))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessageContaining("Попытка зарегистрировать уже существующую ссылку с url = " + "http://dup");
        }
    }

    @Nested
    @DisplayName("fetchLinksToUpdate()")
    class FetchToUpdate {
        @Test
        @DisplayName("нет устаревших → пустой список")
        void noExpired() {
            // Act & Assert
            assertThat(repo.fetchLinksToUpdate(60, 5)).isEmpty();
        }

        @Test
        @DisplayName("возвращает только устаревшие, обновляя last_checked")
        void returnsExpiredOnly() {
            // Arrange
            jdbc.sql(
                            """
                  INSERT INTO link (original_url, last_modified, last_checked, version, type)
                  VALUES ('old', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '120 seconds', 0, 'GITHUB')
                """)
                    .update();
            jdbc.sql(
                            """
                  INSERT INTO link (original_url, last_modified, last_checked, version, type)
                  VALUES ('fresh', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '30 seconds', 0, 'GITHUB')
                """)
                    .update();

            // Act
            List<Link> batch = repo.fetchLinksToUpdate(60, 5);

            // Assert
            assertThat(batch).hasSize(1).first().extracting(Link::originalUrl).isEqualTo("old");
        }
    }

    @Nested
    @DisplayName("updateLastModified()")
    class UpdateLastModified {
        @Test
        @DisplayName("обновляет last_modified")
        void updatesField() {
            // Arrange
            Long id = jdbc.sql(
                            """
                  INSERT INTO link (original_url, last_modified, last_checked, version, type)
                  VALUES ('z', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB') RETURNING id
                """)
                    .query(Long.class)
                    .single();
            LocalDateTime newTime = LocalDateTime.of(2000, 1, 1, 0, 0);

            // Act
            repo.updateLastModified(id, newTime);

            // Assert
            LocalDateTime got = jdbc.sql("SELECT last_modified FROM link WHERE id = :id")
                    .param("id", id)
                    .query(LocalDateTime.class)
                    .single();
            assertThat(got).isEqualTo(newTime);
        }
    }
}
