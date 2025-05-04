package backend.academy.scrapper.service.base.impl.jpa;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.scrapper.checkupdate.main.LinkUpdater;
import backend.academy.scrapper.converter.base.impl.LinkConverter;
import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.parser.CompositeLinkParser;
import backend.academy.scrapper.repository.jpa.link.LinkJpaRepository;
import backend.academy.scrapper.service.base.LinkService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@ConditionalOnProperty(name = "access-type", havingValue = "ORM")
public class LinkServiceJpaImpl implements LinkService {
    private final LinkJpaRepository linkJpaRepository;
    private final CompositeLinkParser parser;
    private final LinkUpdater linkUpdater;
    private final LinkConverter linkConverter;

    @Override
    public List<Link> fetchBatchToUpdate(int intervalSeconds, int limit) {
        List<Long> ids = linkJpaRepository.fetchIdsToUpdate(intervalSeconds, limit);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Link> links = linkJpaRepository.findAllById(ids);
        links.forEach(link -> link.lastChecked(LocalDateTime.now(ZoneId.systemDefault())));
        return linkJpaRepository.saveAll(links);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Long> findByUrl(String url) {
        return linkJpaRepository.findLinkByOriginalUrl(url).map(Link::id);
    }

    @Override
    public void updateLastModified(Link link) {
        linkJpaRepository.save(link);
    }

    @Override
    public LinkResponse addLink(AddLinkRequest request) {
        return linkJpaRepository
                .findLinkByOriginalUrl(request.link())
                .map(existing -> new LinkResponse(existing.id(), existing.originalUrl(), request.tags()))
                .orElseGet(() -> {
                    Link parsedLink = parser.parse(request.link());
                    UpdateDetail detail = linkUpdater.fetchLastUpdate(parsedLink);
                    parsedLink.lastModified(detail.getCreationTime());
                    Link saved = linkJpaRepository.save(parsedLink);
                    return linkConverter.convert(saved, request.tags());
                });
    }
}
