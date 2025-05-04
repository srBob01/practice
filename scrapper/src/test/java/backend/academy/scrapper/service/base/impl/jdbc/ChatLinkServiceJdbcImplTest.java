package backend.academy.scrapper.service.base.impl.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.repository.jdbc.ChatLinkJdbcRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatLinkServiceJdbcImpl unit‑тесты")
class ChatLinkServiceJdbcImplTest {

    @Mock
    private ChatLinkJdbcRepository chatLinkJdbcRepository;

    @InjectMocks
    private ChatLinkServiceJdbcImpl service;

    @Nested
    @DisplayName("insertChatLink()")
    class InsertChatLink {

        @Test
        @DisplayName("возвращает id, полученный из репозитория")
        void happyPath() {
            // Arrange
            Long chatId = 10L, linkId = 20L;
            when(chatLinkJdbcRepository.insertChatLink(chatId, linkId)).thenReturn(99L);

            // Act
            Long result = service.insertChatLink(chatId, linkId);

            // Assert
            assertThat(result).isEqualTo(99L);
            verify(chatLinkJdbcRepository).insertChatLink(chatId, linkId);
        }
    }

    @Nested
    @DisplayName("deleteChatLink()")
    class DeleteChatLink {

        @Test
        @DisplayName("удаляет без ошибок, когда запись существует")
        void happyPath() {
            // Arrange
            Long chatId = 5L, linkId = 6L;
            when(chatLinkJdbcRepository.deleteChatLink(chatId, linkId)).thenReturn(1);

            // Act
            service.deleteChatLink(chatId, linkId);

            // Assert
            verify(chatLinkJdbcRepository).deleteChatLink(chatId, linkId);
        }

        @Test
        @DisplayName("если запись не найдена – бросает ScrapperException с dev‑сообщением")
        void notFoundThrows() {
            // Arrange
            Long chatId = 7L, linkId = 8L;
            when(chatLinkJdbcRepository.deleteChatLink(chatId, linkId)).thenReturn(0);

            String devMsg = "This link is not registered for this chat:" + chatId;

            // Act & Assert
            assertThatThrownBy(() -> service.deleteChatLink(chatId, linkId))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage(devMsg);

            verify(chatLinkJdbcRepository).deleteChatLink(chatId, linkId);
        }
    }

    @Nested
    @DisplayName("getLinksByChatId()")
    class GetLinksByChatId {

        @Test
        @DisplayName("возвращает список ChatLink из репозитория")
        void returnsList() {
            // Arrange
            Long chatId = 15L;
            ChatLink cl1 = mock(ChatLink.class);
            ChatLink cl2 = mock(ChatLink.class);
            List<ChatLink> list = List.of(cl1, cl2);
            when(chatLinkJdbcRepository.findByChatId(chatId)).thenReturn(list);

            // Act
            List<ChatLink> result = service.getLinksByChatId(chatId);

            // Assert
            assertThat(result).containsExactly(cl1, cl2);
            verify(chatLinkJdbcRepository).findByChatId(chatId);
        }

        @Test
        @DisplayName("возвращает пустой список, если нет записей")
        void returnsEmpty() {
            // Arrange
            Long chatId = 16L;
            when(chatLinkJdbcRepository.findByChatId(chatId)).thenReturn(Collections.emptyList());

            // Act
            List<ChatLink> result = service.getLinksByChatId(chatId);

            // Assert
            assertThat(result).isEmpty();
            verify(chatLinkJdbcRepository).findByChatId(chatId);
        }
    }

    @Nested
    @DisplayName("findIdChatLinkByChatIdAndUrl()")
    class FindIdByChatIdAndUrl {

        @Test
        @DisplayName("возвращает Optional с id, если найдено")
        void found() {
            // Arrange
            Long chatId = 25L;
            String url = "http://example.com";
            when(chatLinkJdbcRepository.findChatLinkIdByChatIdAndUrl(chatId, url))
                    .thenReturn(Optional.of(123L));

            // Act
            Optional<Long> result = service.findIdChatLinkByChatIdAndUrl(chatId, url);

            // Assert
            assertThat(result).contains(123L);
            verify(chatLinkJdbcRepository).findChatLinkIdByChatIdAndUrl(chatId, url);
        }

        @Test
        @DisplayName("возвращает пустой Optional, если нет записи")
        void notFound() {
            // Arrange
            Long chatId = 26L;
            String url = "http://none.com";
            when(chatLinkJdbcRepository.findChatLinkIdByChatIdAndUrl(chatId, url))
                    .thenReturn(Optional.empty());

            // Act
            Optional<Long> result = service.findIdChatLinkByChatIdAndUrl(chatId, url);

            // Assert
            assertThat(result).isEmpty();
            verify(chatLinkJdbcRepository).findChatLinkIdByChatIdAndUrl(chatId, url);
        }
    }

    @Nested
    @DisplayName("deleteChatLinksByTag()")
    class DeleteChatLinksByTag {

        @Test
        @DisplayName("удаляет без ошибок, когда есть записи")
        void happyPath() {
            // Arrange
            Long chatId = 30L;
            String tag = "news";
            when(chatLinkJdbcRepository.deleteChatLinksByTag(chatId, tag)).thenReturn(2);

            // Act
            service.deleteChatLinksByTag(chatId, tag);

            // Assert
            verify(chatLinkJdbcRepository).deleteChatLinksByTag(chatId, tag);
        }

        @Test
        @DisplayName("если нет записей — бросает ScrapperException с dev‑сообщением")
        void notFoundThrows() {
            // Arrange
            Long chatId = 31L;
            String tag = "sports";
            when(chatLinkJdbcRepository.deleteChatLinksByTag(chatId, tag)).thenReturn(0);

            String devMsg = "No chat_link records found for chat id " + chatId + " with tag " + tag;

            // Act & Assert
            assertThatThrownBy(() -> service.deleteChatLinksByTag(chatId, tag))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage(devMsg);

            verify(chatLinkJdbcRepository).deleteChatLinksByTag(chatId, tag);
        }
    }
}
