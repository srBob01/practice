package backend.academy.scrapper.service.base.impl.jdbc;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.repository.jdbc.ChatLinkJdbcRepository;
import backend.academy.scrapper.service.base.ChatLinkService;
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
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class ChatLinkServiceJdbcImpl implements ChatLinkService {
    private final ChatLinkJdbcRepository chatLinkJdbcRepository;

    @Override
    public Long insertChatLink(Long chatId, Long linkId) {
        return chatLinkJdbcRepository.insertChatLink(chatId, linkId);
    }

    @Override
    public void deleteChatLink(Long chatId, Long linkId) {
        int deleted = chatLinkJdbcRepository.deleteChatLink(chatId, linkId);

        if (deleted == 0) {
            throw new ScrapperException("Связь не найдена", "This link is not registered for this chat:" + chatId);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ChatLink> getLinksByChatId(Long chatId) {
        return chatLinkJdbcRepository.findByChatId(chatId);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Long> findIdChatLinkByChatIdAndUrl(Long chatId, String url) {
        return chatLinkJdbcRepository.findChatLinkIdByChatIdAndUrl(chatId, url);
    }

    @Override
    public void deleteChatLinksByTag(Long chatId, String tagName) {
        int deleted = chatLinkJdbcRepository.deleteChatLinksByTag(chatId, tagName);
        if (deleted == 0) {
            throw new ScrapperException(
                    "Связь не найдена", "No chat_link records found for chat id " + chatId + " with tag " + tagName);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Long> getChatIdsByLinkId(Long linkId) {
        return chatLinkJdbcRepository.findChatIdsByLinkId(linkId);
    }
}
