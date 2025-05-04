package backend.academy.bot.service.notification;

import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.service.formatter.LinkFormatter;
import backend.academy.dto.LinkUpdate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Основной сервис обработки входящих обновлений ссылок и отправки уведомлений.
 *
 * <p>В зависимости от режима уведомлений пользователя ({@link UserSession#mode()}), либо отправляет сообщение сразу
 * через {@link TelegramBotSendService}, либо накапливает уведомления в {@link DigestService} для последующего
 * дайджеста.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final UserSessionService sessionService;
    private final DigestService digestService;
    private final TelegramBotSendService sendService;
    private final LinkFormatter formatter;

    /**
     * Обрабатывает входящее обновление, форматирует его и распределяет между пользователями в соответствии с их режимом
     * уведомлений.
     *
     * @param upd объект с данными об обновлении ссылок и списком chatId
     */
    public void handleUpdate(LinkUpdate upd) {
        String formatted = formatter.formatUpdate(upd);
        Set<Long> chatIds = new HashSet<>(upd.chatIds());

        Map<Long, UserSession> sessions = sessionService.multiGet(chatIds);

        for (Long chatId : chatIds) {
            UserSession session = sessions.get(chatId);
            if (session == null) continue;

            switch (session.mode()) {
                case IMMEDIATE -> sendService.sendMessage(chatId, formatted, session);
                case DAILY_DIGEST -> digestService.addUpdate(chatId, formatted);
            }
        }
    }

    /**
     * Получает накопленные уведомления для пользователя и отправляет их единым сообщением.
     *
     * @param chatId идентификатор Telegram-чата
     * @param sess объект сессии пользователя (для формирования клавиатуры при отправке)
     * @param header текст заголовка дайджеста
     */
    public void flushDigest(Long chatId, UserSession sess, String header) {
        List<String> msgs = digestService.fetchAndClear(chatId);

        if (msgs.isEmpty()) {
            return;
        }

        String body = String.join("\n\n", msgs);

        sendService.sendMessage(chatId, header + body, sess);
    }
}
