package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.model.db.tag.Tag;
import backend.academy.scrapper.repository.jpa.tag.TagJpaRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
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
@EntityScan(basePackageClasses = Tag.class)
@EnableJpaRepositories(basePackageClasses = TagJpaRepository.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("TagJpaRepository — интеграционные тесты на Postgres‑контейнере")
class TagJpaRepositoryTestIT {

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
    TagJpaRepository repo;

    @Test
    @DisplayName("insertOrGetIdByName: создаёт и возвращает ID")
    void insertOrGet_createsNew() {
        Long id = repo.insertOrGetIdByName("foo");
        assertThat(id).isGreaterThan(0);
    }

    @Test
    @DisplayName("insertOrGetIdByName: при дубликате возвращает тот же ID")
    void insertOrGet_returnsExisting() {
        Long first = repo.insertOrGetIdByName("dup");
        Long second = repo.insertOrGetIdByName("dup");
        assertThat(second).isEqualTo(first);
    }

    @Test
    @DisplayName("findByName: находит сущность по имени")
    void findByName() {
        Long id = repo.insertOrGetIdByName("xyz");
        Optional<Tag> t = repo.findByName("xyz");
        assertThat(t).isPresent().map(Tag::id).contains(id);
    }
}
