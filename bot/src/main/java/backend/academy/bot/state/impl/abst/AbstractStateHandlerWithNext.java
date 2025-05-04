package backend.academy.bot.state.impl.abst;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;

public abstract class AbstractStateHandlerWithNext extends AbstractStateHandler {
    protected final StateMachine stateMachine;

    protected AbstractStateHandlerWithNext(
            TelegramBotSendService sendService, StateMachine stateMachine, UserSessionService userSessionService) {
        super(sendService, userSessionService);
        this.stateMachine = stateMachine;
    }

    protected void nextStep(UserSession session) {
        State state = session.state();
        State next = stateMachine.next(state);
        session.state(next);
    }
}
