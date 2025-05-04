package backend.academy.bot.service.notification;

import java.util.List;

/** Сервис для накопления и выдачи накопленных уведомлений (дайджестов) пользователей. */
public interface DigestService {

    /**
     * Добавляет одно форматированное уведомление.
     *
     * @param chatId идентификатор Telegram-чата пользователя
     * @param formatted форматированное содержание уведомления
     */
    void addUpdate(long chatId, String formatted);

    /**
     * Получает все накопленные уведомления для данного чата и очищает очередь дайджеста.
     *
     * @param chatId идентификатор Telegram-чата пользователя
     * @return список строк с содержимым всех накопленных уведомлений
     */
    List<String> fetchAndClear(long chatId);
}
