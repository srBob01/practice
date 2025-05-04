package backend.academy.scrapper.service.base.impl.jpa;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.TagRequest;
import backend.academy.scrapper.converter.base.impl.LinkConverter;
import backend.academy.scrapper.converter.base.impl.ListLinksResponseConverter;
import backend.academy.scrapper.converter.jpa.LinkTagConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.linktag.LinkTag;
import backend.academy.scrapper.model.helper.ChatLinkTagPair;
import backend.academy.scrapper.model.helper.LinkChatTag;
import backend.academy.scrapper.repository.jpa.linktag.LinkTagJpaRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@DisplayName("LinkTagServiceJpaImpl unit‑тесты")
class LinkTagServiceJpaImplTest {

    @Mock
    private LinkTagJpaRepository linkTagJpaRepository;

    @Mock
    private LinkTagConverter linkTagConverter;

    @Spy
    private LinkConverter linkConverter;

    @Mock
    private ListLinksResponseConverter listLinksResponseConverter;

    @InjectMocks
    private LinkTagServiceJpaImpl service;

    @Nested
    @DisplayName("insertLinkTag()")
    class InsertLinkTag {

        @Test
        @DisplayName("успешно конвертит и сохраняет новую связь")
        void happyPath() {
            // Arrange
            Long chatLinkId = 1L, tagId = 2L;
            LinkTag entity = mock(LinkTag.class);
            when(linkTagConverter.toEntity(chatLinkId, tagId)).thenReturn(entity);

            // Act
            service.insertLinkTag(chatLinkId, tagId);

            // Assert
            verify(linkTagConverter).toEntity(chatLinkId, tagId);
            verify(linkTagJpaRepository).saveAndFlush(entity);
        }

        @Test
        @DisplayName("при дубликате – бросает ScrapperException с dev‑сообщением")
        void duplicateThrows() {
            // Arrange
            Long chatLinkId = 3L, tagId = 4L;
            LinkTag entity = mock(LinkTag.class);
            when(linkTagConverter.toEntity(chatLinkId, tagId)).thenReturn(entity);
            when(linkTagJpaRepository.saveAndFlush(entity)).thenThrow(new DataIntegrityViolationException("dup"));

            // Act & Assert
            assertThatThrownBy(() -> service.insertLinkTag(chatLinkId, tagId))
                    .isInstanceOf(ScrapperException.class)
                    // ScrapperException хранит dev‑сообщение в getMessage()
                    .hasMessage("Tag already added to this link");

            verify(linkTagConverter).toEntity(chatLinkId, tagId);
            verify(linkTagJpaRepository).saveAndFlush(entity);
        }
    }

    @Nested
    @DisplayName("deleteLinkTag()")
    class DeleteLinkTag {

        @Test
        @DisplayName("удаляет без ошибок, когда связь существует")
        void happyPath() {
            // Arrange
            Long chatLinkId = 5L, tagId = 6L;
            when(linkTagJpaRepository.deleteLinkTagByChatLinkIdAndTagId(chatLinkId, tagId))
                    .thenReturn(1);

            // Act
            service.deleteLinkTag(chatLinkId, tagId);

            // Assert
            verify(linkTagJpaRepository).deleteLinkTagByChatLinkIdAndTagId(chatLinkId, tagId);
        }

        @Test
        @DisplayName("если запись не найдена – бросает ScrapperException с display‑сообщением")
        void notFoundThrows() {
            // Arrange
            Long chatLinkId = 7L, tagId = 8L;
            when(linkTagJpaRepository.deleteLinkTagByChatLinkIdAndTagId(chatLinkId, tagId))
                    .thenReturn(0);

            // Act & Assert
            assertThatThrownBy(() -> service.deleteLinkTag(chatLinkId, tagId))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage("No such tag-link association");

            verify(linkTagJpaRepository).deleteLinkTagByChatLinkIdAndTagId(chatLinkId, tagId);
        }
    }

    @Nested
    @DisplayName("getLinksByTag()")
    class GetLinksByTag {

        @Test
        @DisplayName("конвертит найденные ссылки и возвращает ListLinksResponse")
        void happyPath() {
            // Arrange
            Long chatId = 10L;
            String tag = "news";
            TagRequest req = new TagRequest(chatId, tag);

            TestLink link1 = new TestLink("url1");
            link1.id(11L);
            TestLink link2 = new TestLink("url2");
            link2.id(22L);
            List<Link> rawLinks = List.of(link1, link2);
            when(linkTagJpaRepository.findLinksByTag(chatId, tag)).thenReturn(rawLinks);

            ListLinksResponse sentinel = mock(ListLinksResponse.class);
            when(listLinksResponseConverter.convert(anyList())).thenReturn(sentinel);

            // Act
            ListLinksResponse result = service.getLinksByTag(req);

            // Assert
            assertThat(result).isSameAs(sentinel);
            verify(linkTagJpaRepository).findLinksByTag(chatId, tag);
            verify(linkConverter, times(2)).convert(any());
            verify(listLinksResponseConverter).convert(anyList());
        }
    }

    @Nested
    @DisplayName("getTagsMapForLinks()")
    class GetTagsMapForLinks {

        @Test
        @DisplayName("группирует ChatLinkTagPair по chatLinkId")
        void happyPath() {
            // Arrange
            List<Long> chatLinkIds = List.of(100L, 200L);
            List<ChatLinkTagPair> pairs = List.of(
                    new ChatLinkTagPair(100L, "t1"), new ChatLinkTagPair(100L, "t2"), new ChatLinkTagPair(200L, "x"));
            when(linkTagJpaRepository.findTagsMapForChatLinks(chatLinkIds)).thenReturn(pairs);

            // Act
            Map<Long, List<String>> result = service.getTagsMapForLinks(chatLinkIds);

            // Assert
            assertThat(result).containsExactly(entry(100L, List.of("t1", "t2")), entry(200L, List.of("x")));
            verify(linkTagJpaRepository).findTagsMapForChatLinks(chatLinkIds);
        }
    }

    @Nested
    @DisplayName("getTagsGroupedByLinkAndChat()")
    class GetTagsGroupedByLinkAndChat {

        @Test
        @DisplayName("группирует LinkChatTag по linkId и chatId")
        void happyPath() {
            // Arrange
            List<Long> linkIds = List.of(1L, 2L);
            List<LinkChatTag> triples = List.of(
                    new LinkChatTag(1L, 10L, "a"),
                    new LinkChatTag(1L, 10L, "b"),
                    new LinkChatTag(1L, 20L, "c"),
                    new LinkChatTag(2L, 30L, "z"));
            when(linkTagJpaRepository.findTagsGroupedByLinkAndChat(linkIds)).thenReturn(triples);

            // Act
            Map<Long, Map<Long, List<String>>> result = service.getTagsGroupedByLinkAndChat(linkIds);

            // Assert
            assertThat(result)
                    .containsExactly(
                            entry(
                                    1L,
                                    Map.of(
                                            10L, List.of("a", "b"),
                                            20L, List.of("c"))),
                            entry(2L, Map.of(30L, List.of("z"))));
            verify(linkTagJpaRepository).findTagsGroupedByLinkAndChat(linkIds);
        }
    }

    static class TestLink extends backend.academy.scrapper.model.db.link.Link {
        TestLink(String originalUrl) {
            super(originalUrl);
        }

        @Override
        public backend.academy.scrapper.model.db.link.LinkType getType() {
            return null;
        }
    }
}
