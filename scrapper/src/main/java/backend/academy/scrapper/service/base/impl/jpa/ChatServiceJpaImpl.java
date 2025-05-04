package backend.academy.scrapper.service.base.impl.jpa;

import backend.academy.dto.ChatRequest;
import backend.academy.dto.ChatResponse;
import backend.academy.scrapper.converter.base.impl.ChatEntityToResponseConverter;
import backend.academy.scrapper.converter.base.impl.ChatRequestToEntityConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chat.Chat;
import backend.academy.scrapper.repository.jpa.chat.ChatJpaRepository;
import backend.academy.scrapper.service.base.ChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@ConditionalOnProperty(name = "access-type", havingValue = "ORM")
public class ChatServiceJpaImpl implements ChatService {
    private final ChatRequestToEntityConverter requestChatConverter;
    private final ChatEntityToResponseConverter responseChatConverter;
    private final ChatJpaRepository chatJpaRepository;

    @Override
    public ChatResponse register(ChatRequest chatRequest) {
        Chat chat = requestChatConverter.convert(chatRequest);
        if (chat == null) {
            throw new ScrapperException("Конвертация запроса в чат не удалась", "Converter returned null");
        }
        if (chatJpaRepository.existsById(chat.id())) {
            throw new ScrapperException("Чат с таким ID уже существует", "Chat already exists with id = " + chat.id());
        }
        Chat saved = chatJpaRepository.save(chat);
        return responseChatConverter.convert(saved);
    }

    @Override
    public void unregister(long chatId) {
        if (!chatJpaRepository.existsById(chatId)) {
            throw new ScrapperException("Чат не найден", "Chat with id = " + chatId + " not found");
        }
        chatJpaRepository.deleteById(chatId);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ChatResponse> getAll() {
        return chatJpaRepository.findAll().stream()
                .map(responseChatConverter::convert)
                .toList();
    }
}
