package backend.academy.bot.state.resolver;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.state.resolver.help.StateResolverHelper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Класс, определяющий новое состояние пользователя на основе входящего текста и сессии. Перебирает внедрённые стратегии
 * (отсортированные по @Order) и возвращает результат первой, для которой метод {@code supports(text, session)} вернул
 * {@code true}. Если ни одна стратегия не применима, возвращается {@code INCORRECT_INPUT}.
 */
@Component
@RequiredArgsConstructor
public class StateResolver {
    private final List<StateResolverHelper> resolvers;

    public State resolve(String text, UserSession session) {
        return resolvers.stream()
                .filter(resolver -> resolver.supports(text, session))
                .findFirst()
                .map(resolver -> resolver.resolve(text, session))
                .orElseThrow(() -> new IllegalStateException("No state resolver found"));
    }
}
