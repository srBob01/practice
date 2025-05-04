package backend.academy.bot.service.bot;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.impl.RedisUserSessionService;
import backend.academy.bot.service.preloader.SessionPreloader;
import backend.academy.bot.state.StateHandler;
import backend.academy.bot.state.resolver.StateResolver;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис для обработки обновлений, получаемых от Telegram-бота.
 *
 * <p>Принимает обновления, определяет новое состояние пользователя с помощью {@link StateResolver} и делегирует
 * обработку соответствующему {@link StateHandler}.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TelegramBotHandleService {
    private final TelegramBot bot;
    private final SessionPreloader sessionPreloader;
    private final RedisUserSessionService userSessionService;
    private final Map<State, StateHandler> stateHandlerMap;
    private final StateResolver stateResolver;

    /**
     * Инициализирует прослушивание обновлений от Telegram.
     *
     * <p>Метод запускается после инициализации бина и устанавливает listener для обработки всех получаемых обновлений.
     */
    @PostConstruct
    public void startBotListener() {
        log.info("Starting Telegram Bot listener...");
        sessionPreloader.preloadSessions();
        bot.setUpdatesListener(updates -> {
            log.info("Received {} updates", updates.size());
            for (Update update : updates) {
                handleUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    /**
     * Обрабатывает входящее обновление от Telegram.
     *
     * <p>Если сообщение отсутствует, логируется предупреждение. Иначе происходит:
     *
     * <ul>
     *   <li>Получение или создание сессии пользователя
     *   <li>Определение нового состояния через {@link StateResolver}
     *   <li>Выбор соответствующего обработчика состояния и вызов его метода {@code handle}
     * </ul>
     *
     * @param update входящее обновление от Telegram
     */
    public void handleUpdate(Update update) {
        Message message = update.message();
        if (message == null) {
            log.warn("Message is null");
            return;
        }

        long chatId = message.chat().id();
        String text = message.text();

        // Получаем или создаём сессию пользователя
        UserSession session = userSessionService.get(chatId);

        // Определяем новое состояние на основе текста и сессии
        State newState = stateResolver.resolve(text, session);

        // Если найден обработчик для нового состояния, обновляем состояние сессии и делегируем обработку
        StateHandler handler = stateHandlerMap.get(newState);
        if (handler != null) {
            if (session != null) {
                session.state(newState);
                userSessionService.save(chatId, session);
            }
            log.info(
                    "Handling state for chatId={} with handler: {}",
                    chatId,
                    handler.getClass().getSimpleName());
            handler.handle(chatId, text);
        } else {
            log.warn("No handler found for state: {}", newState);
        }
    }
}
