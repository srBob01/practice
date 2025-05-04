package backend.academy.scrapper.service.base.impl.jdbc.enrich;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.service.base.impl.jdbc.enrich.helper.LinkDetailsFetcher;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@DisplayName("LinkDetailsEnricher — unit tests")
class LinkDetailsEnricherTest {

    @Mock
    LinkDetailsFetcher githubFetcher;

    @Mock
    LinkDetailsFetcher soFetcher;

    LinkDetailsEnricher enricher;

    @BeforeEach
    void setUp() {
        // Arrange: создаём «регистр» фетчеров
        githubFetcher = mock(LinkDetailsFetcher.class);
        soFetcher = mock(LinkDetailsFetcher.class);
        Map<LinkType, LinkDetailsFetcher> fetcherMap = Map.of(
                LinkType.GITHUB, githubFetcher,
                LinkType.STACKOVERFLOW, soFetcher);
        enricher = new LinkDetailsEnricher(fetcherMap);
    }

    @Test
    @DisplayName("пустой вход → пустой выход")
    void emptyList() {
        // Act
        List<Link> result = enricher.enrich(Collections.emptyList());

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(githubFetcher, soFetcher);
    }

    @Nested
    @DisplayName("когда все ссылки одного типа")
    class AllSameType {

        @Test
        @DisplayName("→ вызывается только соответствующий фетчер")
        void allGithubLinks() {
            // Arrange
            Link l1 = mock(Link.class, "g1");
            Link l2 = mock(Link.class, "g2");
            when(l1.getType()).thenReturn(LinkType.GITHUB);
            when(l2.getType()).thenReturn(LinkType.GITHUB);
            List<Link> base = List.of(l1, l2);
            when(githubFetcher.fetchDetails(base)).thenReturn(base);

            // Act
            List<Link> result = enricher.enrich(base);

            // Assert
            assertThat(result).containsExactly(l1, l2);
            verify(githubFetcher).fetchDetails(base);
            verifyNoInteractions(soFetcher);
        }
    }

    @Nested
    @DisplayName("когда ссылки разных типов")
    class MixedTypes {

        @Test
        @DisplayName("→ каждый фетчер обрабатывает свой список")
        void mixedTypes() {
            // Arrange
            Link g = mock(Link.class, "g");
            Link s = mock(Link.class, "s");
            when(g.getType()).thenReturn(LinkType.GITHUB);
            when(s.getType()).thenReturn(LinkType.STACKOVERFLOW);
            List<Link> base = List.of(g, s);

            when(githubFetcher.fetchDetails(List.of(g))).thenReturn(List.of(g));
            when(soFetcher.fetchDetails(List.of(s))).thenReturn(List.of(s));

            // Act
            List<Link> result = enricher.enrich(base);

            // Assert
            assertThat(result).containsExactlyInAnyOrder(g, s);
            verify(githubFetcher).fetchDetails(List.of(g));
            verify(soFetcher).fetchDetails(List.of(s));
        }
    }
}
