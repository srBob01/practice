package backend.academy.bot.keyboard.resolver.help.impl;

import backend.academy.bot.entity.UserSession;
import backend.academy.bot.keyboard.resolver.help.KeyboardGroupByLength;
import backend.academy.bot.keyboard.resolver.help.KeyboardResolverHelper;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Стратегия формирования клавиатуры для пользователей в сессии, но не в процессе ввода */
@Component
@Order(200)
public class SessionKeyboardResolverHelper extends KeyboardResolverHelper {

    public SessionKeyboardResolverHelper(List<String> buttonSessionList, KeyboardGroupByLength keyboardGroupByLength) {
        super(buttonSessionList, keyboardGroupByLength);
    }

    @Override
    public boolean supports(UserSession session) {
        // Поддерживаем, если сессия есть и ни одна другая стратегия не сработала
        return session != null;
    }
}
