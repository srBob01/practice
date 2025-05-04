package backend.academy.scrapper.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.config.LinkBaseMapperConfig;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.repository.jdbc.link.impl.GitHubLinkRepository;
import backend.academy.scrapper.repository.jdbc.mapper.ChatLinkRowMapper;
import backend.academy.scrapper.repository.jdbc.mapper.ChatRowMapper;
import backend.academy.scrapper.repository.jdbc.mapper.LinkRowMapper;
import backend.academy.scrapper.repository.jdbc.mapper.helper.impl.GitHubLinkBaseMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Sql({
    "classpath:db/changelog/changeset/001-create-chat-table.sql",
    "classpath:db/changelog/changeset/002-create-link-table.sql",
    "classpath:db/changelog/changeset/003-create-chat_link-table.sql"
})
@TestPropertySource(properties = "spring.liquibase.enabled=false")
@Import({
    ChatLinkJdbcRepository.class,
    ChatLinkRowMapper.class,
    ChatRowMapper.class,
    LinkRowMapper.class,
    LinkBaseMapperConfig.class,
    GitHubLinkBaseMapper.class,
    GitHubLinkRepository.class
})
@ComponentScan("backend.academy.scrapper.repository.jdbc.mapper.helper")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("ChatLinkJdbcRepository — интеграционные тесты")
class ChatLinkJdbcRepositoryTestIT {

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
    ChatLinkJdbcRepository repo;

    @Test
    @DisplayName("insertChatLink() — happy path")
    void insertHappy() {
        // Arrange — prepare chat and link
        jdbc.sql("INSERT INTO chat(id) VALUES(10)").update();
        jdbc.sql(
                        """
              INSERT INTO link(original_url, last_modified, last_checked, version, type)
              VALUES('u', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='u'")
                .query(Long.class)
                .single();

        // Act — insert chat-link
        Long clId = repo.insertChatLink(10L, linkId);

        // Assert — verify insertion
        assertThat(clId).isPositive();
        int count = jdbc.sql("SELECT count(*) FROM chat_link WHERE id=:id")
                .param("id", clId)
                .query(Integer.class)
                .single();
        assertThat(count).isOne();
    }

    @Test
    @DisplayName("insertChatLink() — duplicate → ScrapperException")
    void insertDuplicate() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(20)").update();
        jdbc.sql(
                        """
              INSERT INTO link(original_url, last_modified, last_checked, version, type)
              VALUES('dup', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='dup'")
                .query(Long.class)
                .single();
        // Act — first insert
        repo.insertChatLink(20L, linkId);

        // Assert — second insert throws
        assertThatThrownBy(() -> repo.insertChatLink(20L, linkId)).isInstanceOf(ScrapperException.class);
    }

    @Test
    @DisplayName("findByChatId() и findChatLinkIdByChatIdAndUrl()")
    void findMethods() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(30)").update();
        jdbc.sql(
                        """
              INSERT INTO link(original_url, last_modified, last_checked, version, type)
              VALUES('url30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """)
                .update();
        long lid = jdbc.sql("SELECT id FROM link WHERE original_url='url30'")
                .query(Long.class)
                .single();
        long clid = repo.insertChatLink(30L, lid);

        // Act — fetch
        List<ChatLink> list = repo.findByChatId(30L);
        Optional<Long> opt = repo.findChatLinkIdByChatIdAndUrl(30L, "url30");

        // Assert
        assertThat(list).hasSize(1);
        ChatLink cl = list.getFirst();
        assertThat(cl.id()).isEqualTo(clid);
        assertThat(cl.chat().id()).isEqualTo(30L);
        assertThat(cl.link().id()).isEqualTo(lid);
        assertThat(cl.link().originalUrl()).isEqualTo("url30");
        assertThat(opt).hasValue(clid);
    }

    @Test
    @DisplayName("deleteChatLink()")
    void deleteChatLink() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(40)").update();
        jdbc.sql(
                        """
              INSERT INTO link(original_url, last_modified, last_checked, version, type)
              VALUES('url40', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """)
                .update();
        long lid = jdbc.sql("SELECT id FROM link WHERE original_url='url40'")
                .query(Long.class)
                .single();
        long clid = repo.insertChatLink(40L, lid);

        // Act — delete
        int deleted = repo.deleteChatLink(40L, lid);

        // Assert — verify deletion count and idempotency
        assertThat(deleted).isOne();
        assertThat(repo.deleteChatLink(40L, lid)).isZero();
    }
}
