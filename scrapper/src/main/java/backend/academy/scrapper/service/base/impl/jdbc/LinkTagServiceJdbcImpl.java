package backend.academy.scrapper.service.base.impl.jdbc;

import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.TagRequest;
import backend.academy.scrapper.converter.base.impl.LinkConverter;
import backend.academy.scrapper.converter.base.impl.ListLinksResponseConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.repository.jdbc.LinkTagJdbcRepository;
import backend.academy.scrapper.service.base.LinkTagService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class LinkTagServiceJdbcImpl implements LinkTagService {
    private final LinkTagJdbcRepository linkTagJdbcRepository;
    private final LinkConverter linkConverter;
    private final ListLinksResponseConverter listLinksResponseConverter;

    @Override
    public void insertLinkTag(Long chatLinkId, Long tagId) {
        linkTagJdbcRepository.insertLinkTag(chatLinkId, tagId);
    }

    @Override
    public void deleteLinkTag(Long chatLinkId, Long tagId) {
        Integer deleted = linkTagJdbcRepository.deleteLinkTag(chatLinkId, tagId);
        if (deleted == 0) {
            throw new ScrapperException("Связь не найдена", "No such tag-link association");
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ListLinksResponse getLinksByTag(TagRequest tagRequest) {
        return listLinksResponseConverter.convert(
                linkTagJdbcRepository.findLinksByTag(tagRequest.chatId(), tagRequest.tag()).stream()
                        .map(linkConverter::convert)
                        .toList());
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<Long, List<String>> getTagsMapForLinks(List<Long> chatLinkIds) {
        return linkTagJdbcRepository.getTagsMapForChatLinks(chatLinkIds);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<Long, Map<Long, List<String>>> getTagsGroupedByLinkAndChat(List<Long> linkIds) {
        return linkTagJdbcRepository.getTagsGroupedByLinkAndChat(linkIds);
    }
}
