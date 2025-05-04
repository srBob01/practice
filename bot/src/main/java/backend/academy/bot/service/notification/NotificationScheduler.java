package backend.academy.bot.service.notification;

import backend.academy.bot.entity.NotificationMode;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.impl.RedisUserSessionService;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Планировщик, выполняющий периодическую отправку ежедневных дайджестов уведомлений.
 *
 * <p>Каждый час в начале часа проверяет все активные пользовательские сессии и, если у пользователя установлен режим
 * {@link NotificationMode#DAILY_DIGEST} и час совпадает с настроенным, инициирует отправку дайджеста через
 * {@link backend.academy.bot.service.notification.NotificationService}.
 */
@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private static final String header = "📬 Your daily digest:\n\n";

    private final RedisUserSessionService sessionService;
    private final NotificationService notificationService;

    /** Запускается каждый час (cron) и проверяет, кому из пользователей нужно отправить накопленные уведомления. */
    @Scheduled(cron = "0 0 * * * *")
    public void onTheHour() {
        int nowHour = LocalTime.now(ZoneId.systemDefault()).getHour();

        Map<Long, UserSession> all = sessionService.findAllSessions();
        for (var e : all.entrySet()) {
            Long chatId = e.getKey();
            UserSession sess = e.getValue();
            if (sess.mode() == NotificationMode.DAILY_DIGEST && sess.digestHour() == nowHour) {
                notificationService.flushDigest(chatId, sess, header);
            }
        }
    }
}
