package backend.academy.scrapper.service.base.impl.it.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.model.db.tag.Tag;
import backend.academy.scrapper.repository.jpa.tag.TagJpaRepository;
import backend.academy.scrapper.service.base.impl.jpa.TagServiceJpaImpl;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TagServiceJpaImpl.class)
@EnableJpaRepositories(basePackageClasses = TagJpaRepository.class)
@EntityScan(basePackageClasses = Tag.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("TagServiceJpaImpl — интеграционные тесты на Postgres‑контейнере")
class TagServiceJpaImplTestIT {

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
        r.add("access-type", () -> "ORM");
    }

    static {
        pg.start();
    }

    @Autowired
    private TagServiceJpaImpl service;

    @Test
    @DisplayName("getOrCreateTagId: создаёт новый и возвращает ID")
    void getOrCreate_createsNew() {
        // Arrange
        String tagName = "foo";

        // Act
        Long id = service.getOrCreateTagId(tagName);

        // Assert
        assertThat(id).isGreaterThan(0);
    }

    @Test
    @DisplayName("getOrCreateTagId: повторный вызов возвращает тот же ID")
    void getOrCreate_returnsExisting() {
        // Arrange
        String tagName = "dup";
        Long first = service.getOrCreateTagId(tagName);

        // Act
        Long second = service.getOrCreateTagId(tagName);

        // Assert
        assertThat(second).isEqualTo(first);
    }

    @Test
    @DisplayName("getTagIdByName: на существующем теге возвращает Optional с ID")
    void getByName_found() {
        // Arrange
        String tagName = "bar";
        Long created = service.getOrCreateTagId(tagName);

        // Act
        Optional<Long> opt = service.getTagIdByName(tagName);

        // Assert
        assertThat(opt).hasValue(created);
    }

    @Test
    @DisplayName("getTagIdByName: на несуществующем теге возвращает пустой Optional")
    void getByName_missing() {
        // Arrange
        String tagName = "no_such";

        // Act
        Optional<Long> opt = service.getTagIdByName(tagName);

        // Assert
        assertThat(opt).isEmpty();
    }
}
