package backend.academy.scrapper.service.base.impl.it.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.scrapper.checkupdate.main.LinkUpdater;
import backend.academy.scrapper.converter.base.impl.LinkConverter;
import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.app.update.impl.GitHubUpdateDetail;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.model.db.link.impl.StackOverflowLink;
import backend.academy.scrapper.parser.CompositeLinkParser;
import backend.academy.scrapper.repository.jpa.link.LinkJpaRepository;
import backend.academy.scrapper.service.base.LinkService;
import backend.academy.scrapper.service.base.impl.jpa.LinkServiceJpaImpl;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({LinkServiceJpaImpl.class, LinkConverter.class})
@EntityScan(basePackageClasses = Link.class)
@EnableJpaRepositories(basePackageClasses = LinkJpaRepository.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("LinkServiceJpaImpl — интеграционные тесты")
class LinkServiceJpaImplTestIT {

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
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("access-type", () -> "ORM");
    }

    static {
        pg.start();
    }

    @Autowired
    private LinkService service;

    @Autowired
    private LinkJpaRepository repo;

    @MockitoBean
    private CompositeLinkParser parser;

    @MockitoBean
    private LinkUpdater linkUpdater;

    @Nested
    @DisplayName("fetchBatchToUpdate()")
    class FetchBatch {

        @Test
        @DisplayName("возвращает пустой список, если нечего обновлять")
        void empty() {
            // Arrange
            int interval = 10, limit = 5;

            // Act
            List<Link> batch = service.fetchBatchToUpdate(interval, limit);

            // Assert
            assertThat(batch).isEmpty();
        }

        @Test
        @DisplayName("обновляет lastChecked и возвращает до limit ссылок")
        void happyPath() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            var so = new StackOverflowLink("url1", "q1");
            repo.save(so);
            so.lastChecked(now.minusSeconds(120));
            repo.save(so);
            var gh = new GitHubLink("url2", "owner", "repo", "123", null);
            gh.lastChecked(now.minusSeconds(90));
            repo.save(gh);

            // Act
            List<Link> batch = service.fetchBatchToUpdate(60, 1);

            // Assert
            assertThat(batch).hasSize(1);
            assertThat(batch.getFirst().lastChecked()).isAfter(now.minusSeconds(5));
        }
    }

    @Nested
    @DisplayName("findByUrl()")
    class FindByUrl {

        @Test
        @DisplayName("существующая → возвращает Optional.of(id)")
        void found() {
            // Arrange
            var so = repo.save(new StackOverflowLink("u", "q"));

            // Act
            Optional<Long> id = service.findByUrl("u");

            // Assert
            assertThat(id).contains(so.id());
        }

        @Test
        @DisplayName("не найдена → пустой Optional")
        void notFound() {
            // Arrange / Act / Assert
            assertThat(service.findByUrl("nope")).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateLastModified()")
    class UpdateLastModified {

        @Test
        @DisplayName("сохраняет изменения lastModified")
        void saves() {
            // Arrange
            var so = repo.save(new StackOverflowLink("u2", "q2"));
            so.lastModified(LocalDateTime.of(2000, 1, 1, 0, 0));

            // Act
            service.updateLastModified(so);

            // Assert
            assertThat(repo.findById(so.id()).get().lastModified()).isEqualTo(so.lastModified());
        }
    }

    @Nested
    @DisplayName("addLink()")
    class AddLinkNested {

        @Test
        @DisplayName("существующая ссылка → возвращает новый DTO без парсинга")
        void existing() {
            // Arrange
            var so = repo.save(new StackOverflowLink("exist", "42"));
            var req = new AddLinkRequest("exist", List.of("a", "b"), List.of());

            // Act
            LinkResponse resp = service.addLink(req);

            // Assert
            assertThat(resp.id()).isEqualTo(so.id());
            assertThat(resp.tags()).containsExactly("a", "b");
            assertThat(resp.filters()).isEmpty();
            Mockito.verifyNoInteractions(parser, linkUpdater);
        }

        @Test
        @DisplayName("новая ссылка → парсит, обновляет, сохраняет и конвертит")
        void newLink() {
            // Arrange
            var req = new AddLinkRequest("http://gh/1", List.of("x"), List.of());
            var parsed = new GitHubLink(req.link(), "o", "r", "1", null);
            Mockito.when(parser.parse(req.link())).thenReturn(parsed);
            UpdateDetail detail = new GitHubUpdateDetail("t", "u", LocalDateTime.of(2020, 1, 2, 3, 4, 5), "desc");
            Mockito.when(linkUpdater.fetchLastUpdate(parsed)).thenReturn(detail);

            // Act
            LinkResponse resp = service.addLink(req);

            // Assert
            Link saved = repo.findLinkByOriginalUrl(req.link()).orElseThrow();
            assertThat(saved.lastModified()).isEqualTo(detail.getCreationTime());
            assertThat(resp.id()).isEqualTo(saved.id());
            assertThat(resp.url()).isEqualTo(req.link());
            assertThat(resp.tags()).containsExactlyElementsOf(req.tags());
            assertThat(resp.filters()).isEmpty();
            Mockito.verify(parser).parse(req.link());
            Mockito.verify(linkUpdater).fetchLastUpdate(parsed);
        }
    }
}
