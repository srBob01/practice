package backend.academy.scrapper.service.base.impl.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.scrapper.checkupdate.main.LinkUpdater;
import backend.academy.scrapper.converter.base.impl.LinkConverter;
import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.parser.CompositeLinkParser;
import backend.academy.scrapper.repository.jdbc.link.LinkJdbcRepository;
import backend.academy.scrapper.repository.jdbc.link.impl.LinkImplRepository;
import backend.academy.scrapper.service.base.impl.jdbc.enrich.LinkDetailsEnricher;
import java.time.LocalDateTime;
import java.util.Collections;
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
@DisplayName("LinkServiceJdbcImpl unit‑тесты со Spy‑конвертерами")
class LinkServiceJdbcImplTest {

    @Mock
    private LinkJdbcRepository linkJdbcRepository;

    @Mock
    private LinkUpdater linkUpdater;

    @Mock
    private CompositeLinkParser parser;

    @Mock
    private LinkDetailsEnricher linkDetailsEnricher;

    @Spy
    private LinkConverter linkConverter;

    @Mock
    private Map<LinkType, LinkImplRepository<? extends Link>> linkImplRepositoryMap;

    @Mock
    private LinkImplRepository<Link> linkImplRepo;

    @InjectMocks
    private LinkServiceJdbcImpl service;

    @Nested
    @DisplayName("fetchBatchToUpdate()")
    class FetchBatchToUpdate {

        @Test
        @DisplayName("возвращает пустой список, если нет ссылок для обновления")
        void handlesEmpty() {
            int interval = 60, limit = 5;
            List<Link> empty = Collections.emptyList();

            when(linkJdbcRepository.fetchLinksToUpdate(interval, limit)).thenReturn(empty);
            when(linkDetailsEnricher.enrich(empty)).thenReturn(empty);

            List<Link> result = service.fetchBatchToUpdate(interval, limit);

            assertThat(result).isEmpty();
            verify(linkJdbcRepository).fetchLinksToUpdate(interval, limit);
            verify(linkDetailsEnricher).enrich(empty);
        }

        @Test
        @DisplayName("успешно получает, обогащает и возвращает список ссылок")
        void happyPath() {
            int interval = 30, limit = 10;
            Link l1 = new TestLink("u1");
            Link l2 = new TestLink("u2");
            List<Link> raw = List.of(l1, l2);

            when(linkJdbcRepository.fetchLinksToUpdate(interval, limit)).thenReturn(raw);
            when(linkDetailsEnricher.enrich(raw)).thenReturn(raw);

            List<Link> result = service.fetchBatchToUpdate(interval, limit);

            assertThat(result).containsExactlyElementsOf(raw);
            verify(linkJdbcRepository).fetchLinksToUpdate(interval, limit);
            verify(linkDetailsEnricher).enrich(raw);
        }
    }

    @Nested
    @DisplayName("findByUrl()")
    class FindByUrl {

        @Test
        @DisplayName("возвращает Optional с id, если найдено")
        void returnsIdWhenFound() {
            String url = "http://jdbc.test";
            when(linkJdbcRepository.findIdByUrl(url)).thenReturn(Optional.of(99L));

            Optional<Long> result = service.findByUrl(url);

            assertThat(result).contains(99L);
            verify(linkJdbcRepository).findIdByUrl(url);
        }

        @Test
        @DisplayName("возвращает пустой Optional, если нет записи")
        void returnsEmptyWhenNotFound() {
            String url = "http://jdbc.none";
            when(linkJdbcRepository.findIdByUrl(url)).thenReturn(Optional.empty());

            Optional<Long> result = service.findByUrl(url);

            assertThat(result).isEmpty();
            verify(linkJdbcRepository).findIdByUrl(url);
        }
    }

    @Nested
    @DisplayName("updateLastModified()")
    class UpdateLastModified {

        @Test
        @DisplayName("вызывает обновление lastModified в репозитории")
        void happyPath() {
            Link link = new TestLink("http://any.jdbc");
            link.id(123L);
            LocalDateTime now = LocalDateTime.now();
            link.lastModified(now);

            service.updateLastModified(link);

            verify(linkJdbcRepository).updateLastModified(123L, now);
        }
    }

    @Nested
    @DisplayName("addLink()")
    class AddLinkTests {

        @Test
        @DisplayName("возвращает существующую ссылку без повторных операций")
        void existingLink() {
            String url = "http://exists.jdbc";
            List<String> tags = List.of("a", "b");
            List<String> filters = List.of("f");
            AddLinkRequest req = new AddLinkRequest(url, tags, filters);

            when(linkJdbcRepository.findIdByUrl(url)).thenReturn(Optional.of(55L));

            LinkResponse resp = service.addLink(req);

            assertThat(resp.id()).isEqualTo(55L);
            assertThat(resp.url()).isEqualTo(url);
            assertThat(resp.tags()).isEqualTo(tags);
            assertThat(resp.filters()).isEmpty();

            verify(linkJdbcRepository).findIdByUrl(url);
            verifyNoInteractions(parser, linkUpdater, linkImplRepositoryMap, linkImplRepo, linkConverter);
        }

        @Test
        @DisplayName("добавляет новую ссылку, сохраняет и конвертирует")
        void newLink() {
            // Arrange
            String url = "http://new.jdbc";
            List<String> tags = List.of("t1");
            AddLinkRequest req = new AddLinkRequest(url, tags, List.of());

            when(linkJdbcRepository.findIdByUrl(url)).thenReturn(Optional.empty());

            TestLink parsed = new TestLink(url);
            parsed.id(77L);
            when(parser.parse(url)).thenReturn(parsed);

            UpdateDetail detail = mock(UpdateDetail.class);
            LocalDateTime created = LocalDateTime.of(2021, 5, 10, 12, 30);
            when(detail.getCreationTime()).thenReturn(created);
            when(linkUpdater.fetchLastUpdate(parsed)).thenReturn(detail);

            doNothing().when(linkJdbcRepository).insertLink(parsed);
            doReturn(linkImplRepo).when(linkImplRepositoryMap).get(parsed.getType());
            doNothing().when(linkImplRepo).insert(parsed);

            // Act
            LinkResponse resp = service.addLink(req);

            // Assert
            assertThat(resp.id()).isEqualTo(77L);
            verify(linkJdbcRepository).insertLink(parsed);
            verify(linkImplRepositoryMap).get(parsed.getType());
            verify(linkImplRepo).insert(parsed);
            verify(linkConverter).convert(parsed, tags);
        }
    }

    /** Утилитный класс для моков — не требует реальной реализации. */
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
