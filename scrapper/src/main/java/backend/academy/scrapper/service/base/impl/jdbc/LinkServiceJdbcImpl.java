package backend.academy.scrapper.service.base.impl.jdbc;

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
import backend.academy.scrapper.service.base.LinkService;
import backend.academy.scrapper.service.base.impl.jdbc.enrich.LinkDetailsEnricher;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class LinkServiceJdbcImpl implements LinkService {
    private final LinkJdbcRepository linkJdbcRepository;
    private final LinkUpdater linkUpdater;
    private final CompositeLinkParser parser;
    private final LinkDetailsEnricher linkDetailsEnricher;
    private final LinkConverter linkConverter;
    private final Map<LinkType, LinkImplRepository<? extends Link>> linkImplRepositoryMap;

    @Override
    public List<Link> fetchBatchToUpdate(int intervalSeconds, int limit) {
        List<Link> links = linkJdbcRepository.fetchLinksToUpdate(intervalSeconds, limit);
        return linkDetailsEnricher.enrich(links);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Long> findByUrl(String url) {
        return linkJdbcRepository.findIdByUrl(url);
    }

    @Override
    @Transactional
    public void updateLastModified(Link link) {
        linkJdbcRepository.updateLastModified(link.id(), link.lastModified());
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public LinkResponse addLink(AddLinkRequest request) {
        return linkJdbcRepository
                .findIdByUrl(request.link())
                .map(existingId -> new LinkResponse(existingId, request.link(), request.tags()))
                .orElseGet(() -> {
                    Link link = parser.parse(request.link());
                    UpdateDetail updateDetail = linkUpdater.fetchLastUpdate(link);
                    link.lastModified(updateDetail.getCreationTime());

                    linkJdbcRepository.insertLink(link);
                    ((LinkImplRepository<Link>) linkImplRepositoryMap.get(link.getType())).insert(link);

                    return linkConverter.convert(link, request.tags());
                });
    }
}
