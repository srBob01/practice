package backend.academy.scrapper.repository.jdbc.link.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(GitHubLinkRepository.class)
@Sql({
    "classpath:db/changelog/changeset/002-create-link-table.sql",
    "classpath:db/changelog/changeset/006-create-github_link-table.sql"
})
@TestPropertySource(properties = "spring.liquibase.enabled=false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("GitHubLinkRepository — интеграционные тесты")
class GitHubLinkRepositoryTestIT {

    @SuppressWarnings("resource")
    @SuppressFBWarnings(
            value = "RV_RESOURCE_LEAK",
            justification = "PostgreSQLContainer lifecycle managed by Testcontainers JUnit extension")
    @ServiceConnection
    @Container
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    JdbcClient jdbc;

    @Autowired
    GitHubLinkRepository repo;

    @Nested
    @DisplayName("insert()")
    class Insert {

        @Test
        @DisplayName("happy path")
        void happyPath() {
            // Arrange: вставляем «базовую» запись в link
            jdbc.sql(
                            """
                        INSERT INTO link
                          (original_url, last_modified, last_checked, version, type)
                        VALUES
                          ('http://dummy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
                        """)
                    .update();

            // моделируем доменный объект с тем же id
            var link = new GitHubLink("http://dummy", "owner1", "repo1", "42", GitHubEventType.ISSUE);
            link.id(1L);

            // Act: сохраняем в дочернюю таблицу
            repo.insert(link);

            // Assert: данные сохранились корректно
            String owner = jdbc.sql("SELECT owner FROM github_link WHERE id = :id")
                    .param("id", 1L)
                    .query(String.class)
                    .single();
            assertThat(owner).isEqualTo("owner1");
        }

        @Test
        @DisplayName("дубликат → ScrapperException")
        void duplicateThrows() {
            // Arrange: создаём link и уже существующую запись в github_link
            jdbc.sql(
                            """
                        INSERT INTO link
                          (original_url, last_modified, last_checked, type)
                        VALUES
                          ('http://dup', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'GITHUB')
                        """)
                    .update();
            jdbc.sql(
                            """
                        INSERT INTO github_link
                          (id, owner, repo, item_number, event_type)
                        VALUES
                          (1, 'o', 'r', '1', 'ISSUE')
                        """)
                    .update();

            var bad = new GitHubLink("http://dup", "x", "y", "9", GitHubEventType.PR);
            bad.id(1L);

            // Act & Assert: при повторной вставке — ScrapperException
            assertThatThrownBy(() -> repo.insert(bad)).isInstanceOf(ScrapperException.class);
        }
    }

    @Nested
    @DisplayName("findByIds()")
    class FindByIds {

        @Test
        @DisplayName("заполняет все поля из БД")
        void fillsFields() {
            // Arrange
            jdbc.sql(
                            """
                        INSERT INTO link
                          (original_url, last_modified, last_checked, version, type)
                        VALUES
                          ('http://x', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
                        """)
                    .update();
            jdbc.sql(
                            """
                        INSERT INTO github_link
                          (id, owner, repo, item_number, event_type)
                        VALUES
                          (1, 'own', 'rep', '77', 'PR')
                        """)
                    .update();

            var fresh = new GitHubLink();
            fresh.id(1L);

            // Act
            repo.findByIds(List.of(1L), Map.of(1L, fresh));

            // Assert
            assertThat(fresh.owner()).isEqualTo("own");
            assertThat(fresh.repo()).isEqualTo("rep");
            assertThat(fresh.itemNumber()).isEqualTo("77");
            assertThat(fresh.eventType()).isEqualTo(GitHubEventType.PR);
        }
    }
}
