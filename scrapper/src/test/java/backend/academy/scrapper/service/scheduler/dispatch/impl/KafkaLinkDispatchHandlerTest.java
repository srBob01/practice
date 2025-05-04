package backend.academy.scrapper.service.scheduler.dispatch.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.checkupdate.main.LinkUpdater;
import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.outbox.OutboxMessage;
import backend.academy.scrapper.service.base.ChatLinkService;
import backend.academy.scrapper.service.base.LinkService;
import backend.academy.scrapper.service.base.OutboxService;
import backend.academy.scrapper.service.serialization.JsonSerializationService;
import com.fasterxml.jackson.core.JsonProcessingException;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaLinkDispatchHandler (outbox pattern)")
class KafkaLinkDispatchHandlerTest {

    private static final String TOPIC = "test-topic";

    @Mock
    private LinkService linkService;

    @Mock
    private ChatLinkService chatLinkService;

    @Mock
    private LinkUpdater linkUpdater;

    @Mock
    private OutboxService outboxService;

    @Mock
    private JsonSerializationService jsonSer;

    @Captor
    private ArgumentCaptor<OutboxMessage> outboxCaptor;

    private KafkaLinkDispatchHandler handler;

    @BeforeEach
    void setUp() {
        handler =
                new KafkaLinkDispatchHandler(linkService, chatLinkService, linkUpdater, outboxService, jsonSer, TOPIC);
    }

    // Простая реализация Link для тестов
    static class TestLink extends Link {
        public TestLink(String originalUrl) {
            super(originalUrl);
        }

        @Override
        public LinkType getType() {
            return LinkType.GITHUB;
        }
    }

    // Простая реализация UpdateDetail для тестов
    static class SimpleDetail implements UpdateDetail {
        private final LocalDateTime time;
        private final String desc;

        public SimpleDetail(LocalDateTime time, String desc) {
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
    @DisplayName("new update — сохраняет сообщение в outbox и обновляет ссылку")
    void shouldDispatchWhenNewUpdateAvailable() throws JsonProcessingException {
        // Arrange
        TestLink link = new TestLink("url");
        link.id(1L);
        link.lastModified(null);
        LocalDateTime now = LocalDateTime.now();
        UpdateDetail detail = new SimpleDetail(now, "desc");
        when(linkUpdater.fetchLastUpdate(link)).thenReturn(detail);
        when(chatLinkService.getChatIdsByLinkId(1L)).thenReturn(List.of(10L, 20L));
        LinkUpdate dto = new LinkUpdate(1L, "url", "desc", List.of(10L, 20L));
        when(jsonSer.toJson(dto)).thenReturn("json");

        // Act
        handler.handleOne(link);

        // Assert
        verify(linkService).updateLastModified(link);
        verify(outboxService).save(outboxCaptor.capture());
        OutboxMessage msg = outboxCaptor.getValue();
        assertEquals(TOPIC, msg.topic());
        assertEquals("json", msg.payload());
    }

    @Test
    @DisplayName("no update — ничего не сохраняет")
    void shouldNotDispatchWhenNoNewUpdate() {
        // Arrange
        TestLink link = new TestLink("url2");
        link.id(2L);
        LocalDateTime last = LocalDateTime.now();
        link.lastModified(last);
        UpdateDetail detail = new SimpleDetail(last.minusMinutes(1), "desc");
        when(linkUpdater.fetchLastUpdate(link)).thenReturn(detail);

        // Act
        handler.handleOne(link);

        // Assert
        verify(linkService, never()).updateLastModified(any());
        verify(outboxService, never()).save(any());
    }

    @Test
    @DisplayName("exception during fetch — обрабатывает без выброса")
    void shouldHandleExceptionGracefully() {
        // Arrange
        TestLink link = new TestLink("url3");
        link.id(3L);
        when(linkUpdater.fetchLastUpdate(link)).thenThrow(new RuntimeException("err"));

        // Act & Assert
        assertDoesNotThrow(() -> handler.handleOne(link));
        verifyNoInteractions(outboxService);
        verifyNoInteractions(linkService);
    }
}
