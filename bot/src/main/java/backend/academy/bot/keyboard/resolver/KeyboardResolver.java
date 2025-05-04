package backend.academy.bot.keyboard.resolver;

import backend.academy.bot.entity.UserSession;
import backend.academy.bot.keyboard.resolver.help.KeyboardResolverHelper;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Композитный сервис для формирования динамической клавиатуры.
 *
 * <p>Перебирает внедрённые стратегии в порядке, заданном аннотациями @Order, и возвращает клавиатуру от стратегии, для
 * которой {@code supports(session)} возвращает true.
 */
@Component
@RequiredArgsConstructor
public class KeyboardResolver {

    private final List<KeyboardResolverHelper> resolvers;

    public ReplyKeyboardMarkup resolve(UserSession session) {
        return resolvers.stream()
                .filter(resolver -> resolver.supports(session))
                .findFirst()
                .map(KeyboardResolverHelper::resolve)
                .orElseThrow(() -> new IllegalStateException("No state resolver found"));
    }
}
