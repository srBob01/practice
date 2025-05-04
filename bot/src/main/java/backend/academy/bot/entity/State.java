package backend.academy.bot.entity;

import lombok.Getter;

@Getter
public enum State {
    // Состояния, определяемые по команде (содержат синтаксис)
    START("/start", true, false),
    WITHOUT_START(null, false, false),
    HELP("/help", true, true),
    LIST("/list", true, true),
    TRACK("/track", true, true),
    UNTRACK("/untrack", true, true),
    ADD_TAG("/add_tag", true, true),
    DELETE_TAG("/delete_tag", true, true),
    LINKS_BY_TAG("/links_by_tag", true, true),
    DELETE_LINKS_BY_TAG("/delete_links_by_tag", true, true),
    UNREGISTER("/unregister", true, true),
    GET_MODE("/get_mode", true, true),
    SET_IMMEDIATE("/set_immediate", true, true),
    SET_DAILY_DIGEST("/set_daily_digest", true, true),
    SET_DIGEST_HOUR("/set_digest_hour", true, true),

    // Состояния ожидания ввода (не определяются по команде)
    T_WAIT_LINK(null, false, false),
    T_WAIT_TAG(null, false, false),
    T_WAIT_FILTERS(null, false, false),
    U_WAIT_LINK(null, false, false),
    A_WAIT_TAG(null, false, false),
    D_WAIT_TAG(null, false, false),
    L_WAIT_TAG(null, false, false),
    DL_WAIT_TAG(null, false, false),
    S_WAIT_HOUR(null, false, false),

    // Состояние для выхода из состояния ожидания
    END("/end", false, false),

    // Состояние ошибки
    INCORRECT_INPUT(null, false, false);

    private final String command; // Фактическая команда (например, "/start")
    private final boolean isCommandDefined; // Есть ли обработчик под команду
    private final boolean isCommandToDisplay; // Нужно ли отображать команду в минибаре

    State(String command, boolean isCommandDefined, boolean isCommandToDisplay) {
        this.command = command;
        this.isCommandDefined = isCommandDefined;
        this.isCommandToDisplay = isCommandToDisplay;
    }
}
