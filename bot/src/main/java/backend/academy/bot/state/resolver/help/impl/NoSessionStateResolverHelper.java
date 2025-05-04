package backend.academy.bot.state.resolver.help.impl;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.state.resolver.help.StateResolverHelper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Стратегия разрешения состояния для случаев, когда сессия пользователя отсутствует.
 *
 * <p>Если сессия равна {@code null}, то:
 *
 * <ul>
 *   <li>Если входящий текст пустой или не соответствует команде {@code START} (определяемой по карте команд),
 *       возвращается состояние {@code WITHOUT_START}.
 *   <li>Если текст соответствует команде {@code START}, возвращается состояние {@code START}.
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NoSessionStateResolverHelper implements StateResolverHelper {

    private final Map<String, State> commandToStateMap;

    @Override
    public boolean supports(String text, UserSession session) {
        return session == null;
    }

    @Override
    public State resolve(String text, UserSession session) {
        // Если текст пустой или не соответствует команде START, возвращаем WITHOUT_START; иначе START.
        if (text == null || text.isBlank() || commandToStateMap.get(text) != State.START) {
            return State.WITHOUT_START;
        }
        return State.START;
    }
}
