package backend.academy.bot.state.resolver.help.impl;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.state.resolver.help.StateResolverHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Стратегия разрешения состояния для зарегистрированных пользователей, когда входящий текст не является командой.
 *
 * <p>Если сессия присутствует и текст не начинается с {@code "/"}:
 *
 * <ul>
 *   <li>Если пользователь находится в режиме ожидания (определяемом через {@code stateMachine.isWait(prevState)}),
 *       возвращается текущее состояние;
 *   <li>В противном случае возвращается {@code INCORRECT_INPUT}.
 * </ul>
 */
@Component
@Order(200)
@RequiredArgsConstructor
public class NonCommandTextStateResolverHelper implements StateResolverHelper {

    private final StateMachine stateMachine;

    @Override
    public boolean supports(String text, UserSession session) {
        return session != null && (text == null || (!text.isBlank() && !text.startsWith("/")));
    }

    @Override
    public State resolve(String text, UserSession session) {
        State prevState = session.state();
        // Если пользователь находится в режиме ожидания, сохраняем текущее состояние, иначе INCORRECT_INPUT.
        return (prevState != null && stateMachine.isWait(prevState)) ? prevState : State.INCORRECT_INPUT;
    }
}
