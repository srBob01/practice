package backend.academy.scrapper.service.base.impl.jdbc;

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
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.repository.jdbc.LinkTagJdbcRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("LinkTagServiceJdbcImpl unit‑тесты")
class LinkTagServiceJdbcImplTest {

    @Mock
    private LinkTagJdbcRepository linkTagJdbcRepository;

    @Spy
    private LinkConverter linkConverter;

    @Mock
    private ListLinksResponseConverter listLinksResponseConverter;

    @InjectMocks
    private LinkTagServiceJdbcImpl service;

    @Nested
    @DisplayName("insertLinkTag()")
    class InsertLinkTag {

        @Test
        @DisplayName("вызывает insertLinkTag в репозитории")
        void happyPath() {
            // Arrange
            Long chatLinkId = 1L, tagId = 2L;

            // Act
            service.insertLinkTag(chatLinkId, tagId);

            // Assert
            verify(linkTagJdbcRepository).insertLinkTag(chatLinkId, tagId);
        }
    }

    @Nested
    @DisplayName("deleteLinkTag()")
    class DeleteLinkTag {

        @Test
        @DisplayName("удаляет без ошибок, когда удалилось > 0")
        void happyPath() {
            // Arrange
            Long chatLinkId = 3L, tagId = 4L;
            when(linkTagJdbcRepository.deleteLinkTag(chatLinkId, tagId)).thenReturn(1);

            // Act
            service.deleteLinkTag(chatLinkId, tagId);

            // Assert
            verify(linkTagJdbcRepository).deleteLinkTag(chatLinkId, tagId);
        }

        @Test
        @DisplayName("если ничего не удалилось — бросает ScrapperException с dev‑сообщением")
        void notFoundThrows() {
            // Arrange
            Long chatLinkId = 5L, tagId = 6L;
            when(linkTagJdbcRepository.deleteLinkTag(chatLinkId, tagId)).thenReturn(0);

            // Act & Assert
            assertThatThrownBy(() -> service.deleteLinkTag(chatLinkId, tagId))
                    .isInstanceOf(ScrapperException.class)
                    .hasMessage("No such tag-link association");

            verify(linkTagJdbcRepository).deleteLinkTag(chatLinkId, tagId);
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

            TestLink link1 = new TestLink("u1");
            link1.id(11L);
            TestLink link2 = new TestLink("u2");
            link2.id(22L);
            List<Link> raw = List.of(link1, link2);
            when(linkTagJdbcRepository.findLinksByTag(chatId, tag)).thenReturn(raw);

            ListLinksResponse sentinel = mock(ListLinksResponse.class);
            when(listLinksResponseConverter.convert(anyList())).thenReturn(sentinel);

            // Act
            ListLinksResponse result = service.getLinksByTag(req);

            // Assert
            assertThat(result).isSameAs(sentinel);
            verify(linkTagJdbcRepository).findLinksByTag(chatId, tag);
            verify(linkConverter, times(2)).convert(any());
            verify(listLinksResponseConverter).convert(anyList());
        }
    }

    @Nested
    @DisplayName("getTagsMapForLinks()")
    class GetTagsMapForLinks {

        @Test
        @DisplayName("возвращает карту из репозитория")
        void happyPath() {
            // Arrange
            List<Long> chatLinkIds = List.of(100L, 200L);
            Map<Long, List<String>> map = Map.of(
                    100L, List.of("a", "b"),
                    200L, List.of("x"));
            when(linkTagJdbcRepository.getTagsMapForChatLinks(chatLinkIds)).thenReturn(map);

            // Act
            Map<Long, List<String>> result = service.getTagsMapForLinks(chatLinkIds);

            // Assert
            assertThat(result).isSameAs(map);
            verify(linkTagJdbcRepository).getTagsMapForChatLinks(chatLinkIds);
        }
    }

    @Nested
    @DisplayName("getTagsGroupedByLinkAndChat()")
    class GetTagsGroupedByLinkAndChat {

        @Test
        @DisplayName("возвращает вложенную карту из репозитория")
        void happyPath() {
            // Arrange
            List<Long> linkIds = List.of(1L, 2L);
            Map<Long, Map<Long, List<String>>> nested = Map.of(
                    1L, Map.of(11L, List.of("t1"), 12L, List.of("t2")),
                    2L, Map.of(21L, List.of("x")));
            when(linkTagJdbcRepository.getTagsGroupedByLinkAndChat(linkIds)).thenReturn(nested);

            // Act
            Map<Long, Map<Long, List<String>>> result = service.getTagsGroupedByLinkAndChat(linkIds);

            // Assert
            assertThat(result).isSameAs(nested);
            verify(linkTagJdbcRepository).getTagsGroupedByLinkAndChat(linkIds);
        }
    }

    static class TestLink extends Link {
        TestLink(String originalUrl) {
            super(originalUrl);
        }

        @Override
        public LinkType getType() {
            return null;
        }
    }
}
