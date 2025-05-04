package backend.academy.bot.state.resolver.help;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.UserSession;

/**
 * Интерфейс для стратегий разрешения состояния пользователя.
 *
 * <p>Предоставляет два метода:
 *
 * <ul>
 *   <li>{@code supports(String, UserSession)} – проверяет, применима ли данная стратегия для входящего текста и сессии;
 *   <li>{@code resolve(String, UserSession)} – если стратегия применима, разрешает состояние пользователя.
 * </ul>
 */
public interface StateResolverHelper {

    /**
     * Проверяет, применима ли данная стратегия для указанного текста и сессии.
     *
     * @param text входящий текст сообщения
     * @param session сессия пользователя (может быть {@code null}, если пользователь не зарегистрирован)
     * @return {@code true}, если стратегия применима, иначе {@code false}
     */
    boolean supports(String text, UserSession session);

    /**
     * Разрешает состояние пользователя на основе входящего текста и сессии. Предполагается, что метод вызывается только
     * если {@code supports(...)} вернул {@code true}.
     *
     * @param text входящий текст сообщения
     * @param session сессия пользователя
     * @return разрешённое состояние
     */
    State resolve(String text, UserSession session);
}
