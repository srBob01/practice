package backend.academy.bot.service.base;

import backend.academy.bot.entity.UserSession;
import java.util.Map;
import java.util.Set;

/**
 * Сервис для управления сессиями пользователей в боте.
 *
 * <p>Определяет операции создания, получения, обновления и удаления пользовательских сессий, а также массовую выборку и
 * поиск всех активных сессий.
 */
public interface UserSessionService {

    /**
     * Создаёт новую сессию для пользователя с заданным chatId.
     *
     * @param chatId уникальный идентификатор чата пользователя
     */
    void createSession(Long chatId);

    /**
     * Возвращает сессию пользователя по chatId.
     *
     * @param chatId уникальный идентификатор чата пользователя
     * @return объект UserSession или null, если сессия не найдена
     */
    UserSession get(Long chatId);

    /**
     * Сохраняет или обновляет текущую сессию пользователя.
     *
     * @param chatId уникальный идентификатор чата пользователя
     * @param session объект UserSession с новыми данными
     */
    void save(Long chatId, UserSession session);

    /**
     * Удаляет сессию пользователя по chatId.
     *
     * @param chatId уникальный идентификатор чата пользователя
     */
    void remove(Long chatId);

    /**
     * Возвращает map сессий для заданного набора chatId.
     *
     * @param chatIds набор идентификаторов чатов пользователей
     * @return карта chatId → UserSession для найденных сессий
     */
    Map<Long, UserSession> multiGet(Set<Long> chatIds);

    /**
     * Возвращает все активные пользовательские сессии.
     *
     * @return карта chatId → UserSession для всех активных сессий
     */
    Map<Long, UserSession> findAllSessions();
}
