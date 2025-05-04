package backend.academy.scrapper.service.base.impl.jpa;

import backend.academy.scrapper.converter.jpa.ChatLinkConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.repository.jpa.chatlink.ChatLinkJpaRepository;
import backend.academy.scrapper.service.base.ChatLinkService;
import java.util.List;
import java.util.Optional;
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
public class ChatLinkServiceJpaImpl implements ChatLinkService {
    private final ChatLinkJpaRepository chatLinkJpaRepository;
    private final ChatLinkConverter chatLinkConverter;

    @Override
    public Long insertChatLink(Long chatId, Long linkId) {
        try {
            ChatLink chatLink = chatLinkConverter.toEntity(chatId, linkId);
            return chatLinkJpaRepository.save(chatLink).id();
        } catch (DataIntegrityViolationException e) {
            throw new ScrapperException(
                    "Ссылка для данного пользователя уже добавлена",
                    "User with id: " + chatId + " already subscribed to link " + linkId);
        }
    }

    @Override
    public void deleteChatLink(Long chatId, Long linkId) {
        int deleted = chatLinkJpaRepository.deleteChatLinkByChatIdAndLinkId(chatId, linkId);
        if (deleted == 0) {
            throw new ScrapperException("Связь не найдена", "This link is not registered for this chat:" + chatId);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ChatLink> getLinksByChatId(Long chatId) {
        return chatLinkJpaRepository.findChatLinkByChatId(chatId);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Long> findIdChatLinkByChatIdAndUrl(Long chatId, String url) {
        return chatLinkJpaRepository
                .findChatLinkByChatIdAndLink_OriginalUrl(chatId, url)
                .map(ChatLink::id);
    }

    @Override
    public void deleteChatLinksByTag(Long chatId, String tagName) {
        int deleted = chatLinkJpaRepository.deleteChatLinksByTag(chatId, tagName);
        if (deleted == 0) {
            throw new ScrapperException(
                    "Связь не найдена", "No chat_link records found for chat id " + chatId + " with tag " + tagName);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Long> getChatIdsByLinkId(Long linkId) {
        return chatLinkJpaRepository.findChatIdsByLinkId(linkId);
    }
}
