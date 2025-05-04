package backend.academy.scrapper.service.base.impl.jdbc;

import backend.academy.dto.ChatRequest;
import backend.academy.dto.ChatResponse;
import backend.academy.scrapper.converter.base.impl.ChatEntityToResponseConverter;
import backend.academy.scrapper.converter.base.impl.ChatRequestToEntityConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chat.Chat;
import backend.academy.scrapper.repository.jdbc.ChatJdbcRepository;
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
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class ChatServiceJdbcImpl implements ChatService {
    private final ChatRequestToEntityConverter requestChatConverter;
    private final ChatEntityToResponseConverter responseChatConverter;

    private final ChatJdbcRepository chatJdbcRepository;

    @Override
    public ChatResponse register(ChatRequest chatRequest) {
        Chat chat = requestChatConverter.convert(chatRequest);
        if (chat == null) {
            throw new ScrapperException("Конвертация запроса в чат не удалась", "Converter returned null");
        }
        return responseChatConverter.convert(chatJdbcRepository.addChat(chat));
    }

    @Override
    public void unregister(long chatId) {
        int deleted = chatJdbcRepository.deleteChat(chatId);
        if (deleted == 0) {
            throw new ScrapperException("Чат не найден", "Попытка удалить несуществующий чат с id = " + chatId);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ChatResponse> getAll() {
        return chatJdbcRepository.findAll().stream()
                .map(responseChatConverter::convert)
                .toList();
    }
}
