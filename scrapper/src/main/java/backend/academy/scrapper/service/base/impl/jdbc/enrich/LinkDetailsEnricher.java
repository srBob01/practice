package backend.academy.scrapper.service.base.impl.jdbc.enrich;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.service.base.impl.jdbc.enrich.helper.LinkDetailsFetcher;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkDetailsEnricher {
    private final Map<LinkType, LinkDetailsFetcher> linkTypeLinkDetailsFetcherMap;

    public List<Link> enrich(List<Link> baseLinks) {
        return baseLinks.stream().collect(Collectors.groupingBy(Link::getType)).entrySet().stream()
                .flatMap(entry -> {
                    LinkType type = entry.getKey();
                    List<Link> links = entry.getValue();
                    LinkDetailsFetcher fetcher = linkTypeLinkDetailsFetcherMap.get(type);
                    return fetcher.fetchDetails(links).stream();
                })
                .toList();
    }
}
