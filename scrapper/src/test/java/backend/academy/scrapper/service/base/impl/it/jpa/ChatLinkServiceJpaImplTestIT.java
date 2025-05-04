package backend.academy.scrapper.service.base.impl.it.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.converter.jpa.ChatLinkConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chat.Chat;
import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import backend.academy.scrapper.model.db.linktag.LinkTag;
import backend.academy.scrapper.model.db.tag.Tag;
import backend.academy.scrapper.service.base.ChatLinkService;
import backend.academy.scrapper.service.base.impl.jpa.ChatLinkServiceJpaImpl;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Import({ChatLinkServiceJpaImpl.class, ChatLinkConverter.class})
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("ChatLinkServiceJpaImpl — интеграционные тесты ORM")
class ChatLinkServiceJpaImplTestIT {

    @SuppressWarnings("resource")
    @SuppressFBWarnings(
            value = "RV_RESOURCE_LEAK",
            justification = "Testcontainers lifecycle managed by JUnit extension")
    @Container
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PG::getJdbcUrl);
        registry.add("spring.datasource.username", PG::getUsername);
        registry.add("spring.datasource.password", PG::getPassword);
        registry.add("access-type", () -> "ORM");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("access-type", () -> "ORM");
    }

    static {
        PG.start();
    }

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ChatLinkService service;

    private Long chatId;
    private GitHubLink link;

    @BeforeEach
    void setUp() {
        // Arrange общие Chat и Link
        chatId = 100L;
        em.persist(new Chat(chatId));

        String url = "https://github.com/owner/repo/issues/42";
        link = new GitHubLink(url, "owner", "repo", "42", GitHubEventType.ISSUE);
        em.persist(link);

        em.flush();
    }

    @Nested
    @DisplayName("insertChatLink()")
    class Insert {

        @Test
        @DisplayName("happy path")
        void happyPath() {
            // Act
            Long clId = service.insertChatLink(chatId, link.id());
            // Assert
            assertThat(clId).isPositive();
        }

        @Test
        @DisplayName("duplicate → ScrapperException")
        void duplicateThrows() {
            // Arrange
            service.insertChatLink(chatId, link.id());
            // Act & Assert
            assertThatThrownBy(() -> service.insertChatLink(chatId, link.id())).isInstanceOf(ScrapperException.class);
        }
    }

    @Nested
    @DisplayName("getLinksByChatId() & findIdChatLinkByChatIdAndUrl()")
    class Find {

        @Test
        @DisplayName("возвращает список и корректный ID по URL")
        void returnsListAndOptional() {
            // Arrange
            Long clId = service.insertChatLink(chatId, link.id());
            // Act
            List<ChatLink> list = service.getLinksByChatId(chatId);
            Optional<Long> opt = service.findIdChatLinkByChatIdAndUrl(chatId, link.originalUrl());
            // Assert
            assertThat(list).hasSize(1);
            ChatLink cl = list.getFirst();
            assertThat(cl.id()).isEqualTo(clId);
            assertThat(opt).hasValue(clId);
        }
    }

    @Nested
    @DisplayName("deleteChatLink()")
    class Delete {

        @Test
        @DisplayName("happy path")
        void happyPath() {
            // Arrange
            Long clId = service.insertChatLink(chatId, link.id());
            // Act
            service.deleteChatLink(chatId, link.id());
            // Assert
            assertThat(service.getLinksByChatId(chatId)).isEmpty();
        }

        @Test
        @DisplayName("non-existent → ScrapperException")
        void notFoundThrows() {
            // Act & Assert
            assertThatThrownBy(() -> service.deleteChatLink(chatId, 999L)).isInstanceOf(ScrapperException.class);
        }
    }

    @Nested
    @DisplayName("deleteChatLinksByTag()")
    class DeleteByTag {

        @Test
        @DisplayName("happy path")
        void happyPath() {
            // Arrange — создаём ChatLink и привязываем Tag
            Long clId = service.insertChatLink(chatId, link.id());
            ChatLink cl = em.find(ChatLink.class, clId);

            Tag tag = new Tag(null, "test-tag");
            em.persist(tag);

            LinkTag lt = new LinkTag();
            lt.chatLink(cl);
            lt.tag(tag);
            em.persist(lt);

            em.flush();

            // Act
            service.deleteChatLinksByTag(chatId, "test-tag");
            // Assert
            assertThat(service.getLinksByChatId(chatId)).isEmpty();
        }

        @Test
        @DisplayName("non-existent tag → ScrapperException")
        void notFoundThrows() {
            // Act & Assert
            assertThatThrownBy(() -> service.deleteChatLinksByTag(chatId, "no-such-tag"))
                    .isInstanceOf(ScrapperException.class);
        }
    }
}
