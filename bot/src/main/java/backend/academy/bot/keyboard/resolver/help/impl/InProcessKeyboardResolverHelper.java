package backend.academy.bot.keyboard.resolver.help.impl;

import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.keyboard.resolver.help.KeyboardGroupByLength;
import backend.academy.bot.keyboard.resolver.help.KeyboardResolverHelper;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Стратегия формирования клавиатуры для пользователей, находящихся в процессе (например, подписки/отписки).
 *
 * <p>В этом случае доступна только команда /end.
 */
@Component
@Order(100)
public class InProcessKeyboardResolverHelper extends KeyboardResolverHelper {

    private final StateMachine stateMachine;

    public InProcessKeyboardResolverHelper(
            StateMachine stateMachine, List<String> buttonInProcessList, KeyboardGroupByLength keyboardGroupByLength) {
        super(buttonInProcessList, keyboardGroupByLength);
        this.stateMachine = stateMachine;
    }

    @Override
    public boolean supports(UserSession session) {
        if (session == null) {
            return false;
        }
        return stateMachine.isWait(session.state());
    }
}
