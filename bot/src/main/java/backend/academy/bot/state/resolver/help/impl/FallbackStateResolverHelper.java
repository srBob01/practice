package backend.academy.bot.state.resolver.help.impl;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.state.resolver.help.StateResolverHelper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Фолбэк-стратегия для разрешения состояния.
 *
 * <p>Если ни одна другая стратегия не сработала, эта стратегия всегда поддерживается и возвращает
 * {@code INCORRECT_INPUT}.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class FallbackStateResolverHelper implements StateResolverHelper {

    @Override
    public boolean supports(String text, UserSession session) {
        return true;
    }

    @Override
    public State resolve(String text, UserSession session) {
        return State.INCORRECT_INPUT;
    }
}
