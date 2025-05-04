package backend.academy.scrapper.service.base.impl.it.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.dto.ChatRequest;
import backend.academy.dto.ChatResponse;
import backend.academy.scrapper.converter.base.impl.ChatEntityToResponseConverter;
import backend.academy.scrapper.converter.base.impl.ChatRequestToEntityConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chat.Chat;
import backend.academy.scrapper.repository.jpa.chat.ChatJpaRepository;
import backend.academy.scrapper.service.base.ChatService;
import backend.academy.scrapper.service.base.impl.jpa.ChatServiceJpaImpl;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@Import({ChatServiceJpaImpl.class, ChatRequestToEntityConverter.class, ChatEntityToResponseConverter.class})
@EnableJpaRepositories(basePackageClasses = ChatJpaRepository.class)
@EntityScan(basePackageClasses = Chat.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("ChatServiceJpaImpl — интеграционные тесты на Postgres‑контейнере")
class ChatServiceJpaImplTestIT {

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
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pg::getJdbcUrl);
        registry.add("spring.datasource.username", pg::getUsername);
        registry.add("spring.datasource.password", pg::getPassword);
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("access-type", () -> "ORM");
    }

    static {
        pg.start();
    }

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatJpaRepository chatRepository;

    @Nested
    @DisplayName("register()")
    class Register {
        @Test
        @DisplayName("happy path сохраняет и возвращает ChatResponse")
        void registerHappyPath() {
            // Arrange
            ChatRequest req = new ChatRequest(123L);

            // Act
            ChatResponse resp = chatService.register(req);

            // Assert
            assertThat(resp.id()).isEqualTo(123L);
            assertThat(chatRepository.findAll()).extracting(Chat::id).containsExactly(123L);
        }

        @Test
        @DisplayName("дублирование ID → ScrapperException")
        void registerDuplicateId() {
            // Arrange
            ChatRequest req = new ChatRequest(5L);
            chatService.register(req);

            // Act & Assert
            assertThatThrownBy(() -> chatService.register(req))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessageContaining("Chat already exists with id = 5");
        }
    }

    @Nested
    @DisplayName("unregister()")
    class Unregister {
        @Test
        @DisplayName("happy path удаляет существующий чат")
        void unregisterHappyPath() {
            // Arrange
            long id = chatService.register(new ChatRequest(777L)).id();

            // Act
            chatService.unregister(id);

            // Assert
            assertThat(chatRepository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("несуществующий ID → ScrapperException")
        void unregisterNotFound() {
            // Arrange
            long missing = 4242L;

            // Act & Assert
            assertThatThrownBy(() -> chatService.unregister(missing))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessageContaining("Chat with id = " + missing + " not found");
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {
        @Test
        @DisplayName("возвращает все чаты")
        void getAllReturnsList() {
            // Arrange
            chatRepository.deleteAll();
            chatService.register(new ChatRequest(1L));
            chatService.register(new ChatRequest(2L));

            // Act
            List<ChatResponse> all = chatService.getAll();

            // Assert
            assertThat(all).extracting(ChatResponse::id).containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("пустая таблица → пустой список")
        void getAllEmpty() {
            // Arrange
            chatRepository.deleteAll();

            // Act
            List<ChatResponse> all = chatService.getAll();

            // Assert
            assertThat(all).isEmpty();
        }
    }
}
