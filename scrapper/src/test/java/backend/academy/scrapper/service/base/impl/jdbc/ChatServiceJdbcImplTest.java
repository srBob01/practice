package backend.academy.scrapper.service.base.impl.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.dto.ChatRequest;
import backend.academy.dto.ChatResponse;
import backend.academy.scrapper.converter.base.impl.ChatEntityToResponseConverter;
import backend.academy.scrapper.converter.base.impl.ChatRequestToEntityConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chat.Chat;
import backend.academy.scrapper.repository.jdbc.ChatJdbcRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatServiceJdbcImpl unit‑тесты со Spy‑конвертерами")
class ChatServiceJdbcImplTest {

    @Spy
    ChatEntityToResponseConverter responseChatConverter;

    @Spy
    ChatRequestToEntityConverter requestChatConverter;

    @Mock
    ChatJdbcRepository repo;

    @InjectMocks
    ChatServiceJdbcImpl service;

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("успешно конвертит и сохраняет, возвращает ChatResponse")
        void happyPath() {
            // Arrange
            ChatRequest req = new ChatRequest(42L);
            when(repo.addChat(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));
            // Act
            ChatResponse result = service.register(req);

            // Assert
            assertThat(result.id()).isEqualTo(42L);
            verify(requestChatConverter).convert(req);
            verify(repo).addChat(any(Chat.class));
            verify(responseChatConverter).convert(any(Chat.class));
        }

        @Test
        @DisplayName("если конвертер вернул null – бросает ScrapperException")
        void nullConversionThrows() {
            // Arrange
            ChatRequest req = new ChatRequest(99L);
            doReturn(null).when(requestChatConverter).convert(req);

            // Act & Assert
            assertThatThrownBy(() -> service.register(req))
                    .isInstanceOf(ScrapperException.class)
                    // ScrapperException.getMessage() возвращает техническую строку
                    .hasMessage("Converter returned null");

            verify(requestChatConverter).convert(req);
            verifyNoInteractions(repo, responseChatConverter);
        }
    }

    @Nested
    @DisplayName("unregister()")
    class Unregister {

        @Test
        @DisplayName("успешно удаляет чат, если есть удалённые строки")
        void happyPath() {
            // Arrange
            when(repo.deleteChat(123L)).thenReturn(1);

            // Act
            service.unregister(123L);

            // Assert
            verify(repo).deleteChat(123L);
        }

        @Test
        @DisplayName("бросает ScrapperException, если чат не найден")
        void notFoundThrows() {
            // Arrange
            when(repo.deleteChat(5L)).thenReturn(0);

            // Act & Assert
            assertThatThrownBy(() -> service.unregister(5L))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage("Попытка удалить несуществующий чат с id = 5");

            verify(repo).deleteChat(5L);
        }

        @Test
        @DisplayName("прокидывает RuntimeException, если репозиторий упал")
        void repoErrorPropagates() {
            // Arrange
            doThrow(new IllegalArgumentException("no such")).when(repo).deleteChat(5L);

            // Act & Assert
            assertThatThrownBy(() -> service.unregister(5L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("no such");

            verify(repo).deleteChat(5L);
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("конвертирует каждый Chat → ChatResponse")
        void returnsMappedList() {
            // Arrange
            Chat c1 = new Chat().id(1L);
            Chat c2 = new Chat().id(2L);
            when(repo.findAll()).thenReturn(List.of(c1, c2));

            // Act
            List<ChatResponse> list = service.getAll();

            // Assert
            assertThat(list).extracting(ChatResponse::id).containsExactlyInAnyOrder(1L, 2L);
            verify(repo).findAll();
            verify(responseChatConverter, times(2)).convert(any(Chat.class));
        }

        @Test
        @DisplayName("возвращает пустой список, если нет чатов")
        void handlesEmpty() {
            // Arrange
            when(repo.findAll()).thenReturn(List.of());

            // Act
            List<ChatResponse> list = service.getAll();

            // Assert
            assertThat(list).isEmpty();
            verify(repo).findAll();
        }
    }
}
