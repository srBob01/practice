package backend.academy.scrapper.converter.jpa;

import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.model.db.linktag.LinkTag;
import backend.academy.scrapper.model.db.tag.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class LinkTagConverter {
    @PersistenceContext
    private EntityManager entityManager;

    public LinkTag toEntity(Long chatLinkId, Long tagId) {
        ChatLink chatLink = entityManager.getReference(ChatLink.class, chatLinkId);
        Tag tag = entityManager.getReference(Tag.class, tagId);
        LinkTag linkTag = new LinkTag();
        linkTag.chatLink(chatLink);
        linkTag.tag(tag);
        return linkTag;
    }
}
