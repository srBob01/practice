package backend.academy.scrapper.service.base.impl.jpa;

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
import backend.academy.scrapper.service.base.LinkTagService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@ConditionalOnProperty(name = "access-type", havingValue = "ORM")
public class LinkTagServiceJpaImpl implements LinkTagService {
    private final LinkTagJpaRepository linkTagJpaRepository;
    private final LinkTagConverter linkTagConverter;
    private final LinkConverter linkConverter;
    private final ListLinksResponseConverter listLinksResponseConverter;

    @Override
    public void insertLinkTag(Long chatLinkId, Long tagId) {
        try {
            LinkTag linkTag = linkTagConverter.toEntity(chatLinkId, tagId);
            linkTagJpaRepository.saveAndFlush(linkTag);
        } catch (DataIntegrityViolationException e) {
            throw new ScrapperException("Связь уже существует", "Tag already added to this link");
        }
    }

    @Override
    public void deleteLinkTag(Long chatLinkId, Long tagId) {
        int deleted = linkTagJpaRepository.deleteLinkTagByChatLinkIdAndTagId(chatLinkId, tagId);
        if (deleted == 0) {
            throw new ScrapperException("Связь не найдена", "No such tag-link association");
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ListLinksResponse getLinksByTag(TagRequest tagRequest) {
        List<Link> links = linkTagJpaRepository.findLinksByTag(tagRequest.chatId(), tagRequest.tag());
        return listLinksResponseConverter.convert(
                links.stream().map(linkConverter::convert).collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<Long, List<String>> getTagsMapForLinks(List<Long> chatLinkIds) {
        List<ChatLinkTagPair> pairs = linkTagJpaRepository.findTagsMapForChatLinks(chatLinkIds);
        return pairs.stream()
                .collect(Collectors.groupingBy(
                        ChatLinkTagPair::chatLinkId,
                        Collectors.mapping(ChatLinkTagPair::tagName, Collectors.toList())));
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<Long, Map<Long, List<String>>> getTagsGroupedByLinkAndChat(List<Long> linkIds) {
        List<LinkChatTag> triples = linkTagJpaRepository.findTagsGroupedByLinkAndChat(linkIds);
        return triples.stream()
                .collect(Collectors.groupingBy(
                        LinkChatTag::linkId,
                        Collectors.groupingBy(
                                LinkChatTag::chatId, Collectors.mapping(LinkChatTag::tag, Collectors.toList()))));
    }
}
