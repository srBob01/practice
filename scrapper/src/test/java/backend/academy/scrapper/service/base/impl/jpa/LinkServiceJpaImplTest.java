package backend.academy.scrapper.service.base.impl.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.scrapper.checkupdate.main.LinkUpdater;
import backend.academy.scrapper.converter.base.impl.LinkConverter;
import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.parser.CompositeLinkParser;
import backend.academy.scrapper.repository.jpa.link.LinkJpaRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
@DisplayName("LinkServiceJpaImpl unit‑тесты со Spy‑конвертерами")
class LinkServiceJpaImplTest {

    @Mock
    private LinkJpaRepository linkJpaRepository;

    @Mock
    private CompositeLinkParser parser;

    @Mock
    private LinkUpdater linkUpdater;

    @Spy
    private LinkConverter linkConverter;

    @InjectMocks
    private LinkServiceJpaImpl service;

    @Nested
    @DisplayName("fetchBatchToUpdate()")
    class FetchBatchToUpdate {

        @Test
        @DisplayName("возвращает пустой список, если нет id для обновления")
        void handlesEmpty() {
            // Arrange
            int interval = 60, limit = 5;
            when(linkJpaRepository.fetchIdsToUpdate(interval, limit)).thenReturn(Collections.emptyList());

            // Act
            List<Link> result = service.fetchBatchToUpdate(interval, limit);

            // Assert
            assertThat(result).isEmpty();
            verify(linkJpaRepository).fetchIdsToUpdate(interval, limit);
            verify(linkJpaRepository, never()).findAllById(any());
            verify(linkJpaRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("успешно получает, обновляет и сохраняет ссылки")
        void happyPath() {
            // Arrange
            int interval = 60, limit = 5;
            List<Long> ids = List.of(1L, 2L);
            when(linkJpaRepository.fetchIdsToUpdate(interval, limit)).thenReturn(ids);

            Link link1 = new TestLink("url1");
            Link link2 = new TestLink("url2");
            List<Link> links = List.of(link1, link2);
            when(linkJpaRepository.findAllById(ids)).thenReturn(links);
            when(linkJpaRepository.saveAll(links)).thenReturn(links);

            // Act
            List<Link> result = service.fetchBatchToUpdate(interval, limit);

            // Assert
            assertThat(result).containsExactlyElementsOf(links);
            verify(linkJpaRepository).fetchIdsToUpdate(interval, limit);
            verify(linkJpaRepository).findAllById(ids);
            verify(linkJpaRepository).saveAll(links);
        }
    }

    @Nested
    @DisplayName("findByUrl()")
    class FindByUrl {

        @Test
        @DisplayName("возвращает Optional с id, если ссылка найдена")
        void returnsIdWhenFound() {
            // Arrange
            String url = "http://example.com";
            Link link = new TestLink(url);
            link.id(123L);
            when(linkJpaRepository.findLinkByOriginalUrl(url)).thenReturn(Optional.of(link));

            // Act
            Optional<Long> result = service.findByUrl(url);

            // Assert
            assertThat(result).contains(123L);
            verify(linkJpaRepository).findLinkByOriginalUrl(url);
        }

        @Test
        @DisplayName("возвращает пустой Optional, если ссылка не найдена")
        void returnsEmptyWhenNotFound() {
            // Arrange
            String url = "http://notfound.com";
            when(linkJpaRepository.findLinkByOriginalUrl(url)).thenReturn(Optional.empty());

            // Act
            Optional<Long> result = service.findByUrl(url);

            // Assert
            assertThat(result).isEmpty();
            verify(linkJpaRepository).findLinkByOriginalUrl(url);
        }
    }

    @Nested
    @DisplayName("updateLastModified()")
    class UpdateLastModified {

        @Test
        @DisplayName("вызывает save в репозитории")
        void happyPath() {
            // Arrange
            Link link = new TestLink("http://any");
            // Act
            service.updateLastModified(link);

            // Assert
            verify(linkJpaRepository).save(link);
        }
    }

    @Nested
    @DisplayName("addLink()")
    class AddLinkTests {

        @Test
        @DisplayName("возвращает существующую ссылку без повторного сохранения")
        void existingLink() {
            // Arrange
            String url = "http://exists.com";
            List<String> tags = List.of("t1", "t2");
            List<String> filters = List.of("f1");
            AddLinkRequest req = new AddLinkRequest(url, tags, filters);

            Link existing = new TestLink(url);
            existing.id(50L);
            existing.originalUrl(url);

            when(linkJpaRepository.findLinkByOriginalUrl(url)).thenReturn(Optional.of(existing));

            // Act
            LinkResponse resp = service.addLink(req);

            // Assert
            assertThat(resp.id()).isEqualTo(50L);
            assertThat(resp.url()).isEqualTo(url);
            assertThat(resp.tags()).isEqualTo(tags);
            assertThat(resp.filters()).isEmpty();

            verify(linkJpaRepository).findLinkByOriginalUrl(url);
            verify(parser, never()).parse(any());
            verify(linkUpdater, never()).fetchLastUpdate(any());
            verify(linkJpaRepository, never()).save(any(Link.class));
            verify(linkConverter, never()).convert(any(), any());
        }

        @Test
        @DisplayName("добавляет новую ссылку, сохраняет и конвертирует")
        void newLink() {
            // Arrange
            String url = "http://new.com";
            List<String> tags = List.of("tag");
            List<String> filters = List.of("filter");
            AddLinkRequest req = new AddLinkRequest(url, tags, filters);

            when(linkJpaRepository.findLinkByOriginalUrl(url)).thenReturn(Optional.empty());

            Link parsed = new TestLink(url);
            parsed.id(77L);
            when(parser.parse(url)).thenReturn(parsed);

            UpdateDetail detail = mock(UpdateDetail.class);
            when(detail.getCreationTime()).thenReturn(LocalDateTime.of(2020, 1, 1, 0, 0));
            when(linkUpdater.fetchLastUpdate(parsed)).thenReturn(detail);

            when(linkJpaRepository.save(parsed)).thenReturn(parsed);

            // Act
            LinkResponse resp = service.addLink(req);

            // Assert
            assertThat(resp.id()).isEqualTo(77L);
            assertThat(resp.url()).isEqualTo(url);
            assertThat(resp.tags()).isEqualTo(tags);
            assertThat(resp.filters()).isEmpty();

            verify(linkJpaRepository).findLinkByOriginalUrl(url);
            verify(parser).parse(url);
            verify(linkUpdater).fetchLastUpdate(parsed);
            verify(linkJpaRepository).save(parsed);
            verify(linkConverter).convert(parsed, tags);
        }
    }

    static class TestLink extends Link {
        public TestLink(String originalUrl) {
            super(originalUrl);
        }

        @Override
        public LinkType getType() {
            return null;
        }
    }
}
