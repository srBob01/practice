package backend.academy.bot.state.impl.abst;

import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.state.StateHandler;

public abstract class AbstractStateHandler implements StateHandler {
    protected final TelegramBotSendService sendService;
    protected final UserSessionService userSessionService;

    protected AbstractStateHandler(TelegramBotSendService sendService, UserSessionService userSessionService) {
        this.sendService = sendService;
        this.userSessionService = userSessionService;
    }
}
