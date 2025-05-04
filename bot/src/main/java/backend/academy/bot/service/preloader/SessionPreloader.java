package backend.academy.bot.service.preloader;

import backend.academy.bot.config.BotConfig;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.dto.ChatResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Сервис для предзагрузки сессий пользователей при запуске приложения.
 *
 * <p>При старте выполняет запрос к сервису Scrapper по получению списка всех зарегистрированных чатов и создаёт для
 * каждого из них сессию через {@link UserSessionService}. В случае отсутствия чатов или ошибки логирует соответствующее
 * сообщение.
 */
@Slf4j
@Service
public class SessionPreloader {

    private final RestClient restClient;
    private final UserSessionService userSessionService;

    public SessionPreloader(
            RestClient.Builder restClientBuilder, UserSessionService userSessionService, BotConfig botConfig) {
        this.restClient = restClientBuilder.baseUrl(botConfig.scrapperApiUrl()).build();
        this.userSessionService = userSessionService;
    }

    /**
     * Выполняет предзагрузку пользовательских сессий.
     *
     * <p>Делает HTTP GET запрос к endpoint "/tg-chat" сервиса Scrapper, получает список {@link ChatResponse} и создаёт
     * сессии всех найденных чатов. При отсутствии чатов или возникновении ошибки логирует информацию.
     */
    public void preloadSessions() {
        try {
            List<ChatResponse> chats = restClient
                    .get()
                    .uri("/tg-chat") // обновлённый путь
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (chats == null || chats.isEmpty()) {
                log.info("No chats found for session preload.");
                return;
            }

            log.info("Preloading sessions for {} chats", chats.size());
            chats.forEach(chat -> userSessionService.createSession(chat.id()));

        } catch (Exception e) {
            log.error("Failed to preload sessions");
        }
    }
}
