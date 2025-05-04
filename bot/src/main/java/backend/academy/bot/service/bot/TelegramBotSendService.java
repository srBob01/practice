package backend.academy.bot.service.bot;

import backend.academy.bot.entity.UserSession;
import backend.academy.bot.keyboard.resolver.KeyboardResolver;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки сообщений через Telegram-бот.
 *
 * <p>Использует {@link TelegramBot} для выполнения запросов на отправку текста и {@link KeyboardResolver} для
 * формирования разметки клавиатуры в зависимости от состояния {@link UserSession} пользователя.
 */
@RequiredArgsConstructor
@Service
public class TelegramBotSendService {
    /** Экземпляр клиента Telegram-бота для взаимодействия с Telegram API. */
    private final TelegramBot bot;

    /** Компонент для разрешения и формирования встроенной клавиатуры на основе текущей сессии пользователя. */
    private final KeyboardResolver keyboardResolver;

    /**
     * Отправляет текстовое сообщение в указанный чат с разметкой клавиатуры.
     *
     * @param chatId идентификатор чата, куда отправляется сообщение
     * @param message текст сообщения для отправки
     * @param session текущая сессия пользователя, используемая при формировании клавиатуры
     */
    public void sendMessage(long chatId, String message, UserSession session) {
        bot.execute(new SendMessage(chatId, message).replyMarkup(keyboardResolver.resolve(session)));
    }
}
