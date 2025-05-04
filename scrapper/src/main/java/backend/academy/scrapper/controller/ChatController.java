package backend.academy.scrapper.controller;

import backend.academy.dto.ChatRequest;
import backend.academy.dto.ChatResponse;
import backend.academy.scrapper.service.base.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg-chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Возвращает список всех зарегистрированных чатов.
     *
     * @return список DTO {@link ChatResponse} с информацией о зарегистрированных чатах
     */
    @GetMapping
    public List<ChatResponse> getAllChats() {
        return chatService.getAll();
    }

    /**
     * Регистрирует новый чат в системе.
     *
     * @param chatRequest DTO {@link ChatRequest} с данными чата (например, chatId)
     * @return DTO {@link ChatResponse} с подтверждением регистрации
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatResponse registerChat(@Valid @RequestBody ChatRequest chatRequest) {
        return chatService.register(chatRequest);
    }

    /**
     * Удаляет существующий чат по его идентификатору.
     *
     * @param chatId значение заголовка "Tg-Chat-Id" — идентификатор удаляемого чата
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChat(@RequestHeader("Tg-Chat-Id") @NotNull Long chatId) {
        chatService.unregister(chatId);
    }
}
