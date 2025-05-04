package backend.academy.scrapper.service.base.impl.it.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.config.LinkBaseMapperConfig;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.repository.jdbc.ChatLinkJdbcRepository;
import backend.academy.scrapper.repository.jdbc.link.impl.GitHubLinkRepository;
import backend.academy.scrapper.repository.jdbc.mapper.ChatLinkRowMapper;
import backend.academy.scrapper.repository.jdbc.mapper.ChatRowMapper;
import backend.academy.scrapper.repository.jdbc.mapper.LinkRowMapper;
import backend.academy.scrapper.repository.jdbc.mapper.helper.impl.GitHubLinkBaseMapper;
import backend.academy.scrapper.service.base.ChatLinkService;
import backend.academy.scrapper.service.base.impl.jdbc.ChatLinkServiceJdbcImpl;
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
    "classpath:db/changelog/changeset/003-create-chat_link-table.sql",
    "classpath:db/changelog/changeset/004-create-tag-table.sql",
    "classpath:db/changelog/changeset/005-create-link_tag-table.sql"
})
@TestPropertySource(properties = {"spring.liquibase.enabled=false", "access-type=SQL"})
@Import({
    ChatLinkServiceJdbcImpl.class,
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
@DisplayName("ChatLinkServiceJdbcImpl — интеграционные тесты")
class ChatLinkServiceJdbcImplTestIT {

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
    private JdbcClient jdbc;

    @Autowired
    private ChatLinkService service;

    @Test
    @DisplayName("insertChatLink() — happy path")
    void insertChatLink_happyPath() {
        // Arrange: подготовить chat и link
        jdbc.sql("INSERT INTO chat(id) VALUES(100)").update();
        jdbc.sql(
                        """
                INSERT INTO link(original_url, last_modified, last_checked, version, type)
                VALUES('u100', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='u100'")
                .query(Long.class)
                .single();

        // Act: вызвать сервис
        Long clId = service.insertChatLink(100L, linkId);

        // Assert: ID > 0 и запись в БД
        assertThat(clId).isPositive();
        Integer count = jdbc.sql("SELECT count(*) FROM chat_link WHERE id=:id")
                .param("id", clId)
                .query(Integer.class)
                .single();
        assertThat(count).isOne();
    }

    @Test
    @DisplayName("insertChatLink() — duplicate → ScrapperException")
    void insertChatLink_duplicateThrows() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(200)").update();
        jdbc.sql(
                        """
                INSERT INTO link(original_url, last_modified, last_checked, version, type)
                VALUES('dup200', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='dup200'")
                .query(Long.class)
                .single();
        service.insertChatLink(200L, linkId);

        // Act & Assert: при повторной вставке выбрасывается
        assertThatThrownBy(() -> service.insertChatLink(200L, linkId)).isInstanceOf(ScrapperException.class);
    }

    @Test
    @DisplayName("getLinksByChatId() и findIdChatLinkByChatIdAndUrl()")
    void getAndFindByChatId() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(300)").update();
        jdbc.sql(
                        """
                INSERT INTO link(original_url, last_modified, last_checked, version, type)
                VALUES('url300', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='url300'")
                .query(Long.class)
                .single();
        long clId = service.insertChatLink(300L, linkId);

        // Act
        List<ChatLink> list = service.getLinksByChatId(300L);
        Optional<Long> found = service.findIdChatLinkByChatIdAndUrl(300L, "url300");

        // Assert
        assertThat(list).hasSize(1);
        ChatLink cl = list.getFirst();
        assertThat(cl.id()).isEqualTo(clId);
        assertThat(cl.chat().id()).isEqualTo(300L);
        assertThat(cl.link().id()).isEqualTo(linkId);
        assertThat(cl.link().originalUrl()).isEqualTo("url300");
        assertThat(found).hasValue(clId);
    }

    @Test
    @DisplayName("deleteChatLink() — happy path and not found")
    void deleteChatLink_behavior() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(400)").update();
        jdbc.sql(
                        """
                INSERT INTO link(original_url, last_modified, last_checked, version, type)
                VALUES('url400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='url400'")
                .query(Long.class)
                .single();
        service.insertChatLink(400L, linkId);

        // Act & Assert: успешное удаление
        service.deleteChatLink(400L, linkId);
        Integer remaining = jdbc.sql(
                        """
                SELECT count(*) FROM chat_link
                WHERE chat_id=400 AND link_id=:lid
            """)
                .param("lid", linkId)
                .query(Integer.class)
                .single();
        assertThat(remaining).isZero();

        // Act & Assert: попытка удалить снова → ScrapperException
        assertThatThrownBy(() -> service.deleteChatLink(400L, linkId)).isInstanceOf(ScrapperException.class);
    }

    @Test
    @DisplayName("deleteChatLinksByTag() — happy path and not found")
    void deleteByTag_behavior() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(500)").update();
        jdbc.sql(
                        """
                INSERT INTO link(original_url, last_modified, last_checked, version, type)
                VALUES('url500', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='url500'")
                .query(Long.class)
                .single();
        long clId = service.insertChatLink(500L, linkId);

        // создаём тег и связь link_tag
        jdbc.sql("INSERT INTO tag(name) VALUES('t500')").update();
        long tagId = jdbc.sql("SELECT id FROM tag WHERE name='t500'")
                .query(Long.class)
                .single();
        jdbc.sql(
                        """
                INSERT INTO link_tag(chat_link_id, tag_id)
                VALUES(:clid, :tid)
            """)
                .param("clid", clId)
                .param("tid", tagId)
                .update();

        // Act: удалить по тегу
        service.deleteChatLinksByTag(500L, "t500");

        // Assert: связь удалена
        Integer remaining = jdbc.sql("SELECT count(*) FROM chat_link WHERE id=:clid")
                .param("clid", clId)
                .query(Integer.class)
                .single();
        assertThat(remaining).isZero();

        // Act & Assert: повторный вызов → ScrapperException
        assertThatThrownBy(() -> service.deleteChatLinksByTag(500L, "t500")).isInstanceOf(ScrapperException.class);
    }
}
