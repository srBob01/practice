package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.impl.StackOverflowLink;
import backend.academy.scrapper.repository.jpa.link.LinkJpaRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackageClasses = Link.class)
@EnableJpaRepositories(basePackageClasses = LinkJpaRepository.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("LinkJpaRepository — интеграционные тесты")
class LinkJpaRepositoryTestIT {

    @SuppressWarnings("resource")
    @SuppressFBWarnings(
            value = "RV_RESOURCE_LEAK",
            justification = "PostgreSQLContainer lifecycle managed by Testcontainers JUnit extension")
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
        r.add("spring.liquibase.enabled", () -> "false");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    static {
        pg.start();
    }

    @Autowired
    private LinkJpaRepository repo;

    @Nested
    @DisplayName("fetchIdsToUpdate(interval, limit)")
    class FetchIds {

        @Test
        @DisplayName("пустая таблица → пустой список")
        void emptyTable() {
            // Arrange
            int interval = 60;
            int limit = 10;

            // Act
            List<Long> ids = repo.fetchIdsToUpdate(interval, limit);

            // Assert
            assertThat(ids).isEmpty();
        }

        @Test
        @DisplayName("возвращает только устаревшие по lastChecked, с учётом лимита и сортировкой")
        void returnsExpiredOnly() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            var old = new StackOverflowLink("https://so/1", "1");
            repo.save(old);
            old.lastChecked(now.minusSeconds(120));
            repo.save(old);
            var recent = new StackOverflowLink("https://so/2", "2");
            repo.save(recent);
            recent.lastChecked(now.minusSeconds(30));
            repo.save(recent);

            // Act
            List<Long> ids = repo.fetchIdsToUpdate(60, 10);

            // Assert
            assertThat(ids).containsExactly(old.id());
        }

        @Test
        @DisplayName("групповая сортировка и ограничение лимитом")
        void respectsOrderAndLimit() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            var a = new StackOverflowLink("https://so/a", "a");
            repo.save(a);
            a.lastChecked(now.minusSeconds(300));
            repo.save(a);
            var b = new StackOverflowLink("https://so/b", "b");
            repo.save(b);
            b.lastChecked(now.minusSeconds(200));
            repo.save(b);
            var c = new StackOverflowLink("https://so/c", "c");
            repo.save(c);
            c.lastChecked(now.minusSeconds(100));
            repo.save(c);
            // Act
            List<Long> limited = repo.fetchIdsToUpdate(50, 2);

            // Assert
            assertThat(limited).containsExactly(a.id(), b.id());
        }
    }
}
