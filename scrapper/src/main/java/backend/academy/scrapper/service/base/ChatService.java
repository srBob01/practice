package backend.academy.scrapper.service.base;

import backend.academy.dto.ChatRequest;
import backend.academy.dto.ChatResponse;
import java.util.List;

public interface ChatService {
    /**
     * Регистрирует новый чат.
     *
     * @param chatRequest DTO с данными чата (chatId)
     * @return DTO с подтверждением регистрации
     */
    ChatResponse register(ChatRequest chatRequest);

    /**
     * Удаляет чат по его идентификатору.
     *
     * @param chatId идентификатор чата
     */
    void unregister(long chatId);

    /**
     * Возвращает список всех зарегистрированных чатов.
     *
     * @return список DTO с информацией о чатах
     */
    List<ChatResponse> getAll();
}
