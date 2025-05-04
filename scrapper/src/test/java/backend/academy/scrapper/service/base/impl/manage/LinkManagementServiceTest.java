package backend.academy.scrapper.service.base.impl.manage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.TagLinkRequest;
import backend.academy.scrapper.converter.base.impl.LinkConverter;
import backend.academy.scrapper.converter.base.impl.ListLinksResponseConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.service.base.ChatLinkService;
import backend.academy.scrapper.service.base.LinkService;
import backend.academy.scrapper.service.base.LinkTagService;
import backend.academy.scrapper.service.base.TagService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LinkManagementService unit‑тесты")
class LinkManagementServiceTest {

    @Mock
    LinkService linkService;

    @Mock
    ChatLinkService chatLinkService;

    @Mock
    LinkTagService linkTagService;

    @Mock
    TagService tagService;

    @Spy
    LinkConverter linkConverter;

    @Mock
    ListLinksResponseConverter listLinksResponseConverter;

    @InjectMocks
    LinkManagementService service;

    @Nested
    @DisplayName("addLink()")
    class AddLink {

        @Test
        @DisplayName("happy path: создаёт link, chatLink и теговые связи")
        void happyPath() {
            // Arrange
            Long chatId = 1L;
            AddLinkRequest req = new AddLinkRequest("u", List.of("t1", "t2"), List.of());
            LinkResponse lr = new LinkResponse(42L, "u", req.tags());
            when(linkService.addLink(req)).thenReturn(lr);
            when(chatLinkService.insertChatLink(chatId, 42L)).thenReturn(100L);
            when(tagService.getOrCreateTagId("t1")).thenReturn(11L);
            when(tagService.getOrCreateTagId("t2")).thenReturn(22L);

            // Act
            LinkResponse result = service.addLink(chatId, req);

            // Assert
            assertThat(result).isSameAs(lr);
            verify(linkService).addLink(req);
            verify(chatLinkService).insertChatLink(chatId, 42L);
            verify(tagService).getOrCreateTagId("t1");
            verify(tagService).getOrCreateTagId("t2");
            verify(linkTagService).insertLinkTag(100L, 11L);
            verify(linkTagService).insertLinkTag(100L, 22L);
        }

        @Test
        @DisplayName("если сервис addLink бросает исключение, пробрасывается дальше")
        void addLinkThrows() {
            // Arrange
            AddLinkRequest req = new AddLinkRequest("u", List.of(), List.of());
            when(linkService.addLink(req)).thenThrow(new RuntimeException("fail"));

            // Act & Assert
            assertThatThrownBy(() -> service.addLink(1L, req))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("fail");
            verifyNoInteractions(chatLinkService, tagService, linkTagService);
        }
    }

    @Nested
    @DisplayName("getLinksByChatId()")
    class GetLinksByChatId {

        @Test
        @DisplayName("happy path: собирает chatLinks, теги и возвращает DTO")
        void happyPath() {
            // Arrange
            Long chatId = 2L;
            ChatLink cl1 = new ChatLink().id(10L).link(new TestLink("u1"));
            ChatLink cl2 = new ChatLink().id(20L).link(new TestLink("u2"));
            List<ChatLink> cls = List.of(cl1, cl2);
            when(chatLinkService.getLinksByChatId(chatId)).thenReturn(cls);
            Map<Long, List<String>> tagsMap = Map.of(
                    10L, List.of("a"),
                    20L, List.of("b", "c"));
            when(linkTagService.getTagsMapForLinks(List.of(10L, 20L))).thenReturn(tagsMap);
            List<LinkResponse> lrs =
                    List.of(new LinkResponse(0L, "u1", tagsMap.get(10L)), new LinkResponse(0L, "u2", tagsMap.get(20L)));
            when(listLinksResponseConverter.convert(anyList())).thenReturn(new ListLinksResponse(lrs, 2));

            // Act
            ListLinksResponse resp = service.getLinksByChatId(chatId);

            // Assert
            assertThat(resp.size()).isEqualTo(2);
            verify(chatLinkService).getLinksByChatId(chatId);
            verify(linkTagService).getTagsMapForLinks(List.of(10L, 20L));
            verify(linkConverter).convert(cl1.link(), List.of("a"));
            verify(linkConverter).convert(cl2.link(), List.of("b", "c"));
            verify(listLinksResponseConverter).convert(anyList());
        }
    }

    @Nested
    @DisplayName("deleteLink()")
    class DeleteLink {

        @Test
        @DisplayName("happy path: удаляет связь chatLink")
        void happyPath() {
            // Arrange
            Long chatId = 3L;
            String url = "u";
            when(linkService.findByUrl(url)).thenReturn(Optional.of(55L));

            // Act
            service.deleteLink(chatId, url);

            // Assert
            verify(linkService).findByUrl(url);
            verify(chatLinkService).deleteChatLink(chatId, 55L);
        }

        @Test
        @DisplayName("если link не найден — бросает ScrapperException")
        void notFoundThrows() {
            // Arrange
            when(linkService.findByUrl("x")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.deleteLink(1L, "x"))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage("Link with URL not found");
            verifyNoInteractions(chatLinkService);
        }
    }

    @Nested
    @DisplayName("addTagToLink()")
    class AddTagToLink {

        @Test
        @DisplayName("happy path: находит chatLink, создаёт/берёт tag и вставляет связь")
        void happyPath() {
            // Arrange
            TagLinkRequest req = new TagLinkRequest(4L, "u", "t");
            when(chatLinkService.findIdChatLinkByChatIdAndUrl(4L, "u")).thenReturn(Optional.of(99L));
            when(tagService.getOrCreateTagId("t")).thenReturn(77L);

            // Act
            Long result = service.addTagToLink(req);

            // Assert
            assertThat(result).isEqualTo(99L);
            verify(linkTagService).insertLinkTag(99L, 77L);
        }

        @Test
        @DisplayName("если chatLink не найден — бросает ScrapperException")
        void linkNotFoundThrows() {
            // Arrange
            when(chatLinkService.findIdChatLinkByChatIdAndUrl(5L, "u")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.addTagToLink(new TagLinkRequest(5L, "u", "t")))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage("No link found for chat and url");
            verifyNoInteractions(tagService, linkTagService);
        }
    }

    @Nested
    @DisplayName("deleteTagFromLink()")
    class DeleteTagFromLink {

        @Test
        @DisplayName("happy path: находит chatLink и tag, удаляет связь")
        void happyPath() {
            // Arrange
            Long chatId = 6L;
            String url = "u";
            String tag = "t";
            when(chatLinkService.findIdChatLinkByChatIdAndUrl(chatId, url)).thenReturn(Optional.of(123L));
            when(tagService.getTagIdByName(tag)).thenReturn(Optional.of(321L));

            // Act
            service.deleteTagFromLink(chatId, url, tag);

            // Assert
            verify(linkTagService).deleteLinkTag(123L, 321L);
        }

        @Test
        @DisplayName("если chatLink не найден — бросает ScrapperException")
        void linkNotFoundThrows() {
            // Arrange
            when(chatLinkService.findIdChatLinkByChatIdAndUrl(7L, "u")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.deleteTagFromLink(7L, "u", "t"))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage("No link found for chat and url");
            verifyNoInteractions(tagService, linkTagService);
        }

        @Test
        @DisplayName("если tag не найден — бросает ScrapperException")
        void tagNotFoundThrows() {
            // Arrange
            when(chatLinkService.findIdChatLinkByChatIdAndUrl(8L, "u")).thenReturn(Optional.of(444L));
            when(tagService.getTagIdByName("t")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.deleteTagFromLink(8L, "u", "t"))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage("Tag not found: " + "t");
            verifyNoInteractions(linkTagService);
        }
    }

    static class TestLink extends Link {
        TestLink(String url) {
            super(url);
        }

        @Override
        public LinkType getType() {
            return null;
        }
    }
}
