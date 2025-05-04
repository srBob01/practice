package backend.academy.scrapper.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chat.Chat;
import backend.academy.scrapper.repository.jdbc.mapper.ChatRowMapper;
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
@Import({ChatJdbcRepository.class, ChatRowMapper.class})
@Sql(scripts = "classpath:db/changelog/changeset/001-create-chat-table.sql")
@TestPropertySource(properties = "spring.liquibase.enabled=false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("ChatJdbcRepository — интеграционные тесты на Postgres‑контейнере")
class ChatJdbcRepositoryTestIT {

    @SuppressWarnings("resource")
    @SuppressFBWarnings(
            value = "RV_RESOURCE_LEAK",
            justification = "PostgreSQLContainer lifecycle managed by Testcontainers JUnit extension")
    @ServiceConnection
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    ChatJdbcRepository repo;

    @Nested
    @DisplayName("addChat()")
    class AddChat {

        @Test
        @DisplayName("happy path: вставляет и возвращает сущность")
        void happyPath() {
            // Arrange
            Chat chat = new Chat().id(42L);

            // Act
            Chat saved = repo.addChat(chat);

            // Assert
            assertThat(saved.id()).isEqualTo(42L);
            assertThat(repo.findAll()).extracting(Chat::id).containsExactly(42L);
        }

        @Test
        @DisplayName("ошибка дублирования: бросает ScrapperException")
        void duplicateThrows() {
            // Arrange
            Chat chat = new Chat().id(7L);
            repo.addChat(chat);

            // Act & Assert
            assertThatThrownBy(() -> repo.addChat(chat))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessageContaining("Попытка зарегистрировать уже существующий чат");
        }
    }

    @Nested
    @DisplayName("deleteChat()")
    class DeleteChat {

        @Test
        @DisplayName("happy path: удаляет существующую строку")
        void happyPath() {
            // Arrange
            repo.addChat(new Chat().id(1L));

            // Act
            int count = repo.deleteChat(1L);

            // Assert
            assertThat(count).isEqualTo(1);
            assertThat(repo.findAll()).isEmpty();
        }

        @Test
        @DisplayName("ничего не удаляет, если нет такой строки")
        void noSuchRow() {
            // Arrange
            long missingId = 999L;

            // Act
            int count = repo.deleteChat(missingId);

            // Assert
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("пустой список, если нет чатов")
        void empty() {
            // Arrange / Act / Assert
            assertThat(repo.findAll()).isEmpty();
        }

        @Test
        @DisplayName("возвращает все чаты после вставки")
        void returnsAll() {
            // Arrange
            repo.addChat(new Chat().id(11L));
            repo.addChat(new Chat().id(22L));

            // Act
            List<Chat> all = repo.findAll();

            // Assert
            assertThat(all).extracting(Chat::id).containsExactlyInAnyOrder(11L, 22L);
        }
    }
}
