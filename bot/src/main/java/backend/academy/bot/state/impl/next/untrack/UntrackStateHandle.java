package backend.academy.bot.state.impl.next.untrack;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.state.impl.abst.AbstractStateHandlerWithNext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UntrackStateHandle extends AbstractStateHandlerWithNext {

    private static final String SPECIFY_LINK =
            "Please enter the link you want to untrack. To exit the input, enter /end";

    public UntrackStateHandle(
            TelegramBotSendService sendService, StateMachine stateMachine, UserSessionService userSessionService) {
        super(sendService, stateMachine, userSessionService);
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling UNTRACK state for chatId={}", chatId);
        UserSession session = userSessionService.get(chatId);
        nextStep(session);
        userSessionService.save(chatId, session);
        sendService.sendMessage(chatId, SPECIFY_LINK, session);
    }

    @Override
    public State getProcessedState() {
        return State.UNTRACK;
    }
}
