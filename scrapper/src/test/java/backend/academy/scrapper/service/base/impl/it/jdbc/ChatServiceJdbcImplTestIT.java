package backend.academy.scrapper.service.base.impl.it.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.dto.ChatRequest;
import backend.academy.dto.ChatResponse;
import backend.academy.scrapper.converter.base.impl.ChatEntityToResponseConverter;
import backend.academy.scrapper.converter.base.impl.ChatRequestToEntityConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chat.Chat;
import backend.academy.scrapper.repository.jdbc.ChatJdbcRepository;
import backend.academy.scrapper.repository.jdbc.mapper.ChatRowMapper;
import backend.academy.scrapper.service.base.ChatService;
import backend.academy.scrapper.service.base.impl.jdbc.ChatServiceJdbcImpl;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    ChatJdbcRepository.class,
    ChatRowMapper.class,
    ChatServiceJdbcImpl.class,
    ChatRequestToEntityConverter.class,
    ChatEntityToResponseConverter.class
})
@Sql(scripts = "classpath:db/changelog/changeset/001-create-chat-table.sql")
@TestPropertySource(properties = {"spring.liquibase.enabled=false", "access-type=SQL"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("ChatServiceJdbcImpl — интеграционные тесты на Postgres‑контейнере")
class ChatServiceJdbcImplTestIT {

    @SuppressWarnings("resource")
    @SuppressFBWarnings(
            value = "RV_RESOURCE_LEAK",
            justification = "PostgreSQLContainer lifecycle managed by Testcontainers")
    @ServiceConnection
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    ChatService service;

    @Autowired
    ChatJdbcRepository repo;

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("happy path: конвертит, сохраняет и возвращает ChatResponse")
        void happyPath() {
            // Arrange
            ChatRequest request = new ChatRequest(55L);

            // Act
            ChatResponse resp = service.register(request);

            // Assert
            assertThat(resp.id()).isEqualTo(55L);
            assertThat(repo.findAll()).extracting(Chat::id).containsExactly(55L);
        }

        @Test
        @DisplayName("duplicate key: ScrapperException из репозитория")
        void duplicateKey() {
            // Arrange
            ChatRequest request = new ChatRequest(7L);
            service.register(request);

            // Act & Assert
            assertThatThrownBy(() -> service.register(request))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessageContaining("Попытка зарегистрировать уже существующий чат");
        }
    }

    @Nested
    @DisplayName("unregister()")
    class Unregister {

        @Test
        @DisplayName("happy path: удаляет существующий чат")
        void happyPath() {
            // Arrange
            service.register(new ChatRequest(101L));

            // Act
            service.unregister(101L);

            // Assert
            assertThat(repo.findAll()).isEmpty();
        }

        @Test
        @DisplayName("не найден: бросает ScrapperException")
        void notFoundThrows() {
            // Arrange
            long missing = 202L;

            // Act & Assert
            assertThatThrownBy(() -> service.unregister(missing))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage("Попытка удалить несуществующий чат с id = " + missing);
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("happy path: возвращает список ChatResponse")
        void happyPath() {
            // Arrange
            service.register(new ChatRequest(1L));
            service.register(new ChatRequest(2L));

            // Act
            List<ChatResponse> list = service.getAll();

            // Assert
            assertThat(list).extracting(ChatResponse::id).containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("пусто, если нет чатов")
        void empty() {
            // Arrange
            repo.deleteChat(1L);
            repo.deleteChat(2L);

            // Act
            List<ChatResponse> list = service.getAll();

            // Assert
            assertThat(list).isEmpty();
        }
    }
}
