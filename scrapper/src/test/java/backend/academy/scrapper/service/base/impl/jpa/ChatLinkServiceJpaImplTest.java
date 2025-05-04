package backend.academy.scrapper.service.base.impl.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.converter.jpa.ChatLinkConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.repository.jpa.chatlink.ChatLinkJpaRepository;
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
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatLinkServiceJpaImpl unit‑тесты")
class ChatLinkServiceJpaImplTest {

    @Mock
    private ChatLinkJpaRepository chatLinkJpaRepository;

    @Mock
    private ChatLinkConverter chatLinkConverter;

    @InjectMocks
    private ChatLinkServiceJpaImpl service;

    @Nested
    @DisplayName("insertChatLink()")
    class InsertChatLink {

        @Test
        @DisplayName("успешно конвертит, сохраняет и возвращает id")
        void happyPath() {
            // Arrange
            Long chatId = 11L, linkId = 22L;
            ChatLink entity = mock(ChatLink.class);
            when(chatLinkConverter.toEntity(chatId, linkId)).thenReturn(entity);
            when(chatLinkJpaRepository.save(entity)).thenReturn(entity);
            when(entity.id()).thenReturn(42L);

            // Act
            Long result = service.insertChatLink(chatId, linkId);

            // Assert
            assertThat(result).isEqualTo(42L);
            verify(chatLinkConverter).toEntity(chatId, linkId);
            verify(chatLinkJpaRepository).save(entity);
        }

        @Test
        @DisplayName("при дубликате ID – бросает ScrapperException с dev‑сообщением")
        void duplicateThrows() {
            // Arrange
            Long chatId = 7L, linkId = 8L;
            ChatLink entity = mock(ChatLink.class);
            when(chatLinkConverter.toEntity(chatId, linkId)).thenReturn(entity);
            String devMsg = "User with id: " + chatId + " already subscribed to link " + linkId;
            when(chatLinkJpaRepository.save(entity)).thenThrow(new DataIntegrityViolationException("dup"));

            // Act & Assert
            assertThatThrownBy(() -> service.insertChatLink(chatId, linkId))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage(devMsg);

            verify(chatLinkConverter).toEntity(chatId, linkId);
            verify(chatLinkJpaRepository).save(entity);
        }
    }

    @Nested
    @DisplayName("deleteChatLink()")
    class DeleteChatLink {

        @Test
        @DisplayName("удаляет без ошибок, когда запись есть")
        void happyPath() {
            // Arrange
            Long chatId = 100L, linkId = 200L;
            when(chatLinkJpaRepository.deleteChatLinkByChatIdAndLinkId(chatId, linkId))
                    .thenReturn(1);

            // Act
            service.deleteChatLink(chatId, linkId);

            // Assert
            verify(chatLinkJpaRepository).deleteChatLinkByChatIdAndLinkId(chatId, linkId);
        }

        @Test
        @DisplayName("если связь не найдена – бросает ScrapperException с dev‑сообщением")
        void notFoundThrows() {
            // Arrange
            Long chatId = 5L, linkId = 6L;
            when(chatLinkJpaRepository.deleteChatLinkByChatIdAndLinkId(chatId, linkId))
                    .thenReturn(0);

            String devMsg = "This link is not registered for this chat:" + chatId;

            // Act & Assert
            assertThatThrownBy(() -> service.deleteChatLink(chatId, linkId))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage(devMsg);

            verify(chatLinkJpaRepository).deleteChatLinkByChatIdAndLinkId(chatId, linkId);
        }
    }

    @Nested
    @DisplayName("getLinksByChatId()")
    class GetLinksByChatId {

        @Test
        @DisplayName("возвращает список ChatLink из репозитория")
        void returnsList() {
            // Arrange
            Long chatId = 123L;
            ChatLink cl1 = mock(ChatLink.class);
            ChatLink cl2 = mock(ChatLink.class);
            List<ChatLink> list = List.of(cl1, cl2);
            when(chatLinkJpaRepository.findChatLinkByChatId(chatId)).thenReturn(list);

            // Act
            List<ChatLink> result = service.getLinksByChatId(chatId);

            // Assert
            assertThat(result).containsExactly(cl1, cl2);
            verify(chatLinkJpaRepository).findChatLinkByChatId(chatId);
        }

        @Test
        @DisplayName("возвращает пустой список, если нет записей")
        void returnsEmpty() {
            // Arrange
            Long chatId = 321L;
            when(chatLinkJpaRepository.findChatLinkByChatId(chatId)).thenReturn(Collections.emptyList());

            // Act
            List<ChatLink> result = service.getLinksByChatId(chatId);

            // Assert
            assertThat(result).isEmpty();
            verify(chatLinkJpaRepository).findChatLinkByChatId(chatId);
        }
    }

    @Nested
    @DisplayName("findIdChatLinkByChatIdAndUrl()")
    class FindIdByChatIdAndUrl {

        @Test
        @DisplayName("возвращает Optional с id, если связь найдена")
        void found() {
            // Arrange
            Long chatId = 50L;
            String url = "http://test.url";
            ChatLink cl = mock(ChatLink.class);
            when(chatLinkJpaRepository.findChatLinkByChatIdAndLink_OriginalUrl(chatId, url))
                    .thenReturn(Optional.of(cl));
            when(cl.id()).thenReturn(99L);

            // Act
            Optional<Long> result = service.findIdChatLinkByChatIdAndUrl(chatId, url);

            // Assert
            assertThat(result).contains(99L);
            verify(chatLinkJpaRepository).findChatLinkByChatIdAndLink_OriginalUrl(chatId, url);
        }

        @Test
        @DisplayName("возвращает пустой Optional, если нет записи")
        void notFound() {
            // Arrange
            Long chatId = 60L;
            String url = "http://none.url";
            when(chatLinkJpaRepository.findChatLinkByChatIdAndLink_OriginalUrl(chatId, url))
                    .thenReturn(Optional.empty());

            // Act
            Optional<Long> result = service.findIdChatLinkByChatIdAndUrl(chatId, url);

            // Assert
            assertThat(result).isEmpty();
            verify(chatLinkJpaRepository).findChatLinkByChatIdAndLink_OriginalUrl(chatId, url);
        }
    }

    @Nested
    @DisplayName("deleteChatLinksByTag()")
    class DeleteChatLinksByTag {

        @Test
        @DisplayName("удаляет без ошибок, когда есть записи")
        void happyPath() {
            // Arrange
            Long chatId = 7L;
            String tag = "news";
            when(chatLinkJpaRepository.deleteChatLinksByTag(chatId, tag)).thenReturn(2);

            // Act
            service.deleteChatLinksByTag(chatId, tag);

            // Assert
            verify(chatLinkJpaRepository).deleteChatLinksByTag(chatId, tag);
        }

        @Test
        @DisplayName("если нет записей — бросает ScrapperException с dev‑сообщением")
        void notFoundThrows() {
            // Arrange
            Long chatId = 8L;
            String tag = "sports";
            when(chatLinkJpaRepository.deleteChatLinksByTag(chatId, tag)).thenReturn(0);

            String devMsg = "No chat_link records found for chat id " + chatId + " with tag " + tag;

            // Act & Assert
            assertThatThrownBy(() -> service.deleteChatLinksByTag(chatId, tag))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage(devMsg);

            verify(chatLinkJpaRepository).deleteChatLinksByTag(chatId, tag);
        }
    }
}
