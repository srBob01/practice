package backend.academy.bot.validator;

import lombok.Getter;

/** Перечисление приоритетов для валидаторов ссылок. Приоритет используется для определения порядка валидации. */
@Getter
public enum ValidatorPriority {
    FIRST(1),
    SECOND(2);

    private final int cost;

    ValidatorPriority(int cost) {
        this.cost = cost;
    }
}
