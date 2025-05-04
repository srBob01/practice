package backend.academy.bot.keyboard.resolver.help;

import backend.academy.bot.entity.UserSession;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import java.util.List;

/** Интерфейс для стратегий формирования клавиатуры. */
public abstract class KeyboardResolverHelper {

    private static final int MAX_LENGTH_PER_ROW = 35;
    private final ReplyKeyboardMarkup replyKeyboardMarkup;

    protected KeyboardResolverHelper(List<String> buttons, KeyboardGroupByLength keyboardGroupByLength) {
        this.replyKeyboardMarkup = keyboardGroupByLength.groupButtonsByLength(buttons, MAX_LENGTH_PER_ROW);
    }

    /**
     * Проверяет, применима ли данная стратегия для формирования клавиатуры.
     *
     * @param session сессия пользователя (может быть null)
     * @return true, если стратегия поддерживается для данной сессии, иначе false
     */
    public abstract boolean supports(UserSession session);

    /**
     * Формирует клавиатуру для указанной сессии.
     *
     * @return сформированная клавиатура
     */
    public ReplyKeyboardMarkup resolve() {
        return replyKeyboardMarkup;
    }
}
