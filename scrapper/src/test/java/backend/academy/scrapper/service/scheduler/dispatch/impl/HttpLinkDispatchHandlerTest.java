package backend.academy.scrapper.service.scheduler.dispatch.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.checkupdate.main.LinkUpdater;
import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.service.base.ChatLinkService;
import backend.academy.scrapper.service.base.LinkService;
import backend.academy.scrapper.service.client.BotApiClient;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("HTTPLinkDispatchHandler (single calls via HTTP)")
class HttpLinkDispatchHandlerTest {

    @Mock
    private LinkService linkService;

    @Mock
    private ChatLinkService chatLinkService;

    @Mock
    private LinkUpdater linkUpdater;

    @Mock
    private BotApiClient botApiClient;

    @Captor
    private ArgumentCaptor<LinkUpdate> updateCaptor;

    private HttpLinkDispatchHandler handler;

    @BeforeEach
    void setUp() {
        handler = new HttpLinkDispatchHandler(linkService, chatLinkService, linkUpdater, botApiClient);
    }

    // Простая реализация Link
    static class TestLink extends Link {
        public TestLink(String url) {
            super(url);
        }

        @Override
        public LinkType getType() {
            return LinkType.GITHUB;
        }
    }

    // Простая реализация UpdateDetail
    static class SimpleDetail implements UpdateDetail {
        private final LocalDateTime time;
        private final String desc;

        SimpleDetail(LocalDateTime time, String desc) {
            this.time = time;
            this.desc = desc;
        }

        @Override
        public LocalDateTime getCreationTime() {
            return time;
        }

        @Override
        public String getDescription() {
            return desc;
        }
    }

    @Test
    @DisplayName("новый апдейт — вызывает HTTP-клиент и обновляет ссылку")
    void shouldSendHttpWhenNewUpdate() {
        // Arrange
        TestLink link = new TestLink("u");
        link.id(1L);
        link.lastModified(null);
        LocalDateTime now = LocalDateTime.now();
        UpdateDetail detail = new SimpleDetail(now, "desc");
        when(linkUpdater.fetchLastUpdate(link)).thenReturn(detail);
        when(chatLinkService.getChatIdsByLinkId(1L)).thenReturn(List.of(5L));
        Mono<Void> response = Mono.empty();
        when(botApiClient.sendUpdate(any())).thenReturn(response);

        // Act
        handler.handleOne(link);

        // Assert
        verify(linkService).updateLastModified(link);
        verify(botApiClient).sendUpdate(updateCaptor.capture());
        LinkUpdate sent = updateCaptor.getValue();
        assertEquals(1L, sent.id());
        assertEquals("u", sent.url());
        assertEquals("desc", sent.description());
        assertEquals(List.of(5L), sent.chatIds());
    }

    @Test
    @DisplayName("без нового апдейта — не вызывает HTTP")
    void shouldNotSendWhenNoNew() {
        // Arrange
        TestLink link = new TestLink("u2");
        link.id(2L);
        LocalDateTime old = LocalDateTime.now();
        link.lastModified(old);
        UpdateDetail detail = new SimpleDetail(old.minusMinutes(1), "d");
        when(linkUpdater.fetchLastUpdate(link)).thenReturn(detail);

        // Act
        handler.handleOne(link);

        // Assert
        verify(linkService, never()).updateLastModified(any());
        verify(botApiClient, never()).sendUpdate(any());
    }

    @Test
    @DisplayName("ошибка при fetch — обрабатывается без выброса")
    void shouldHandleExceptionGracefully() {
        // Arrange
        TestLink link = new TestLink("u3");
        link.id(3L);
        when(linkUpdater.fetchLastUpdate(link)).thenThrow(new RuntimeException("fail"));

        // Act & Assert
        assertDoesNotThrow(() -> handler.handleOne(link));
        verifyNoInteractions(botApiClient);
        verifyNoInteractions(linkService);
    }
}
