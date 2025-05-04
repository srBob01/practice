package backend.academy.scrapper.repository.jdbc.link.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.impl.StackOverflowLink;
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

@Testcontainers
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(StackOverflowLinkRepository.class)
@Sql({
    "classpath:db/changelog/changeset/002-create-link-table.sql",
    "classpath:db/changelog/changeset/007-create-stackoverflow_link-table.sql"
})
@TestPropertySource(properties = "spring.liquibase.enabled=false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("StackOverflowLinkRepository — интеграционные тесты")
class StackOverflowLinkRepositoryTestIT {

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
    StackOverflowLinkRepository repo;

    @Nested
    @DisplayName("insert()")
    class Insert {

        @Test
        @DisplayName("happy path")
        void happyPath() {
            // Arrange
            jdbc.sql(
                            """
                        INSERT INTO link
                          (original_url, last_modified, last_checked, version, type)
                        VALUES
                          ('http://q', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'STACKOVERFLOW')
                        """)
                    .update();

            var link = new StackOverflowLink("http://q", "Q42");
            link.id(1L);

            // Act
            repo.insert(link);

            // Assert
            String qid = jdbc.sql("SELECT question_id FROM stackoverflow_link WHERE id = :id")
                    .param("id", 1L)
                    .query(String.class)
                    .single();
            assertThat(qid).isEqualTo("Q42");
        }

        @Test
        @DisplayName("дубликат → ScrapperException")
        void duplicateThrows() {
            // Arrange
            jdbc.sql(
                            """
                        INSERT INTO link
                          (original_url, last_modified, last_checked, version, type)
                        VALUES
                          ('http://dup', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'STACKOVERFLOW')
                        """)
                    .update();
            jdbc.sql(
                            """
                        INSERT INTO stackoverflow_link
                          (id, question_id)
                        VALUES
                          (1, 'X')
                        """)
                    .update();

            var bad = new StackOverflowLink("http://dup", "Y");
            bad.id(1L);

            // Act & Assert
            assertThatThrownBy(() -> repo.insert(bad)).isInstanceOf(ScrapperException.class);
        }
    }

    @Nested
    @DisplayName("findByIds()")
    class FindByIds {

        @Test
        @DisplayName("заполняет questionId")
        void fillsQuestionId() {
            // Arrange
            jdbc.sql(
                            """
                        INSERT INTO link
                          (original_url, last_modified, last_checked, version, type)
                        VALUES
                          ('http://x', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'STACKOVERFLOW')
                        """)
                    .update();
            jdbc.sql(
                            """
                        INSERT INTO stackoverflow_link
                          (id, question_id)
                        VALUES
                          (1, 'Q99')
                        """)
                    .update();

            var fresh = new StackOverflowLink();
            fresh.id(1L);

            // Act
            repo.findByIds(List.of(1L), Map.of(1L, fresh));

            // Assert
            assertThat(fresh.questionId()).isEqualTo("Q99");
        }
    }
}
