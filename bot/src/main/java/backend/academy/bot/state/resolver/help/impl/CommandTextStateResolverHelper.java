package backend.academy.bot.state.resolver.help.impl;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.state.resolver.help.StateResolverHelper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Стратегия разрешения состояния для зарегистрированных пользователей, когда входящий текст является командой.
 *
 * <p>Если сессия присутствует и текст начинается с {@code "/"}:
 *
 * <ul>
 *   <li>Если пользователь находится в режиме ожидания (определяемом через {@code stateMachine.isWait(prevState)}), то
 *       единственная допустимая команда – {@code /end} (переводящая в состояние {@code HELP}); для остальных команд
 *       возвращается текущее состояние.
 *   <li>Если пользователь не находится в режиме ожидания, состояние определяется по карте команд; если команда не
 *       найдена, возвращается {@code INCORRECT_INPUT}.
 * </ul>
 */
@Component
@Order(100)
@RequiredArgsConstructor
public class CommandTextStateResolverHelper implements StateResolverHelper {

    private final StateMachine stateMachine;
    private final Map<String, State> commandToStateMap;

    @Override
    public boolean supports(String text, UserSession session) {
        return session != null && text != null && !text.isBlank() && text.startsWith("/");
    }

    @Override
    public State resolve(String text, UserSession session) {
        State prevState = session.state();
        if (prevState != null && stateMachine.isWait(prevState)) {
            // Если пользователь в режиме ожидания, единственная допустимая команда – /end (переводит в HELP),
            // иначе возвращаем предыдущее состояние.
            return State.END.command().equalsIgnoreCase(text) ? State.HELP : prevState;
        }
        // Если не в режиме ожидания, определяем состояние по карте команд.
        return commandToStateMap.getOrDefault(text, State.INCORRECT_INPUT);
    }
}
