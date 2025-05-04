package backend.academy.bot.keyboard.resolver.help.impl;

import backend.academy.bot.entity.UserSession;
import backend.academy.bot.keyboard.resolver.help.KeyboardGroupByLength;
import backend.academy.bot.keyboard.resolver.help.KeyboardResolverHelper;
import java.util.List;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Стратегия формирования клавиатуры для случаев, когда сессия пользователя отсутствует.
 *
 * <p>В этом случае доступна только команда /start.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NoSessionKeyboardResolverHelper extends KeyboardResolverHelper {

    public NoSessionKeyboardResolverHelper(
            List<String> buttonNoSessionList, KeyboardGroupByLength keyboardGroupByLength) {
        super(buttonNoSessionList, keyboardGroupByLength);
    }

    @Override
    public boolean supports(UserSession session) {
        return session == null;
    }
}
