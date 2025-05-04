package backend.academy.scrapper.service.base.impl.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
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
import backend.academy.scrapper.repository.jpa.chat.ChatJpaRepository;
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
@DisplayName("ChatServiceJpaImpl unit‑тесты со Spy‑конвертерами")
class ChatServiceJpaImplTest {

    @Spy
    ChatRequestToEntityConverter requestChatConverter;

    @Spy
    ChatEntityToResponseConverter responseChatConverter;

    @Mock
    ChatJpaRepository repo;

    @InjectMocks
    ChatServiceJpaImpl service;

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("успешно конвертит и сохраняет, возвращает ChatResponse")
        void happyPath() {
            // Arrange
            ChatRequest req = new ChatRequest(42L);
            Chat entity = new Chat().id(42L);
            doReturn(entity).when(requestChatConverter).convert(req);
            when(repo.existsById(42L)).thenReturn(false);
            when(repo.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            ChatResponse resp = service.register(req);

            // Assert
            assertThat(resp.id()).isEqualTo(42L);
            verify(requestChatConverter).convert(req);
            verify(repo).existsById(42L);
            verify(repo).save(entity);
            verify(responseChatConverter).convert(entity);
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
                    .hasMessage("Converter returned null");

            verify(requestChatConverter).convert(req);
            verifyNoInteractions(repo, responseChatConverter);
        }

        @Test
        @DisplayName("при дубликате ID – бросает ScrapperException с сообщением")
        void duplicateIdThrows() {
            // Arrange
            ChatRequest req = new ChatRequest(7L);
            Chat entity = new Chat().id(7L);
            doReturn(entity).when(requestChatConverter).convert(req);
            when(repo.existsById(7L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> service.register(req))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage("Chat already exists with id = 7");

            verify(requestChatConverter).convert(req);
            verify(repo).existsById(7L);
            verify(repo, never()).save(any());
            verifyNoInteractions(responseChatConverter);
        }
    }

    @Nested
    @DisplayName("unregister()")
    class Unregister {

        @Test
        @DisplayName("удаляет без ошибок")
        void happyPath() {
            // Arrange
            when(repo.existsById(42L)).thenReturn(true);

            // Act
            service.unregister(42L);

            // Assert
            verify(repo).existsById(42L);
            verify(repo).deleteById(42L);
        }

        @Test
        @DisplayName("если чат не найден – бросает ScrapperException")
        void notFoundThrows() {
            // Arrange
            when(repo.existsById(5L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> service.unregister(5L))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage("Chat with id = 5 not found");

            verify(repo).existsById(5L);
            verify(repo, never()).deleteById(anyLong());
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
