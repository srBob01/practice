package backend.academy.scrapper.service.base.impl.it.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.dto.AddLinkRequest;
import backend.academy.scrapper.checkupdate.main.LinkUpdater;
import backend.academy.scrapper.config.LinkTypeConfig;
import backend.academy.scrapper.converter.base.impl.LinkConverter;
import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import backend.academy.scrapper.parser.CompositeLinkParser;
import backend.academy.scrapper.repository.jdbc.link.LinkJdbcRepository;
import backend.academy.scrapper.repository.jdbc.link.impl.GitHubLinkRepository;
import backend.academy.scrapper.repository.jdbc.mapper.LinkRowMapper;
import backend.academy.scrapper.service.base.impl.jdbc.LinkServiceJdbcImpl;
import backend.academy.scrapper.service.base.impl.jdbc.enrich.LinkDetailsEnricher;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql({
    "classpath:db/changelog/changeset/002-create-link-table.sql",
    "classpath:db/changelog/changeset/006-create-github_link-table.sql"
})
@Testcontainers
@TestPropertySource(properties = {"spring.liquibase.enabled=false", "access-type=SQL"})
@Import({
    LinkServiceJdbcImpl.class,
    LinkJdbcRepository.class,
    LinkRowMapper.class,
    GitHubLinkRepository.class,
    LinkConverter.class,
    LinkTypeConfig.class
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("LinkServiceJdbcImpl — интеграционные тесты addLink()")
class LinkServiceJdbcImplTestIT {

    @SuppressWarnings("resource")
    @SuppressFBWarnings(
            value = "RV_RESOURCE_LEAK",
            justification = "PostgreSQLContainer lifecycle managed by Testcontainers")
    @ServiceConnection
    @Container
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private LinkServiceJdbcImpl service;

    @MockitoBean
    private CompositeLinkParser parser;

    @MockitoBean
    private LinkUpdater linkUpdater;

    @MockitoBean
    private LinkDetailsEnricher enricher;

    @Test
    @DisplayName("новая GitHub‑ссылка → вставляется в link и github_link, возвращается корректный LinkResponse")
    void addNewGithubLink() {
        String url = "https://github.com/owner/repo/issues/42";
        var parsed = new GitHubLink(url, "owner", "repo", "42", GitHubEventType.ISSUE);
        when(parser.parse(url)).thenReturn(parsed);

        Instant fixed = Instant.parse("2020-01-01T12:34:56Z");
        LocalDateTime ct = LocalDateTime.ofInstant(fixed, ZoneOffset.UTC);
        when(linkUpdater.fetchLastUpdate(parsed)).thenReturn(new TestUpdateDetail(ct));

        var req = new AddLinkRequest(url, List.of("tag1", "tag2"), List.of());
        var resp = service.addLink(req);

        // DTO
        assertThat(resp.id()).isPositive();
        assertThat(resp.url()).isEqualTo(url);
        assertThat(resp.tags()).containsExactly("tag1", "tag2");

        // В link
        Long cnt = jdbc.sql("SELECT COUNT(*) FROM link WHERE id = :id")
                .param("id", resp.id())
                .query(Long.class)
                .single();
        assertThat(cnt).isEqualTo(1);

        // В github_link
        String owner = jdbc.sql("SELECT owner FROM github_link WHERE id = :id")
                .param("id", resp.id())
                .query(String.class)
                .single();
        assertThat(owner).isEqualTo("owner");
    }

    @Test
    @DisplayName("повторный addLink того же URL → не создаётся новой записи, возвращает существующий ID")
    void addDuplicateGithubLink() {
        String url = "https://github.com/owner/repo/issues/42";
        jdbc.sql(
                        """
                    INSERT INTO link(original_url, last_modified, last_checked, version, type)
                    VALUES(:url, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
                """)
                .param("url", url)
                .update();
        Long existingId = jdbc.sql("SELECT id FROM link WHERE original_url = :url")
                .param("url", url)
                .query(Long.class)
                .single();
        jdbc.sql(
                        """
                    INSERT INTO github_link(id, owner, repo, item_number, event_type)
                    VALUES(:id, 'owner','repo','42','ISSUE')
                """)
                .param("id", existingId)
                .update();

        var req = new AddLinkRequest(url, List.of("x"), List.of());
        var resp = service.addLink(req);

        assertThat(resp.id()).isEqualTo(existingId);
        Long cnt = jdbc.sql("SELECT COUNT(*) FROM link WHERE original_url = :url")
                .param("url", url)
                .query(Long.class)
                .single();
        assertThat(cnt).isEqualTo(1);
    }

    /** Тестовая реализация UpdateDetail */
    static class TestUpdateDetail implements UpdateDetail {
        private final LocalDateTime creationTime;

        TestUpdateDetail(LocalDateTime creationTime) {
            this.creationTime = creationTime;
        }

        @Override
        public LocalDateTime getCreationTime() {
            return creationTime;
        }

        @Override
        public String getDescription() {
            return "";
        }
    }
}
