package backend.academy.scrapper.converter.jpa;

import backend.academy.scrapper.model.db.chat.Chat;
import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.model.db.link.Link;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class ChatLinkConverter {

    @PersistenceContext
    private EntityManager entityManager;

    public ChatLink toEntity(Long chatId, Long linkId) {
        Chat chat = entityManager.getReference(Chat.class, chatId);
        Link link = entityManager.getReference(Link.class, linkId);
        ChatLink chatLink = new ChatLink();
        chatLink.chat(chat);
        chatLink.link(link);
        return chatLink;
    }
}
