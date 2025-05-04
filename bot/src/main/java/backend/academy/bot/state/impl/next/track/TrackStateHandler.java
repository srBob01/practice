package backend.academy.bot.state.impl.next.track;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.state.impl.abst.AbstractStateHandlerWithNext;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrackStateHandler extends AbstractStateHandlerWithNext {

    private static final String ENTER_THE_LINK =
            "Please enter the link you want to track. To exit the input, enter /end";

    public TrackStateHandler(
            TelegramBotSendService sendService, StateMachine stateMachine, UserSessionService userSessionService) {
        super(sendService, stateMachine, userSessionService);
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling TRACK state for chatId={}", chatId);
        nextStep(userSessionService.get(chatId));
        UserSession session = userSessionService.get(chatId);
        zeroing(session);
        nextStep(session);
        userSessionService.save(chatId, session);
        sendService.sendMessage(chatId, ENTER_THE_LINK, session);
    }

    private void zeroing(UserSession session) {
        session.link(null);
        session.tags(new ArrayList<>());
        session.filters(new ArrayList<>());
    }

    @Override
    public State getProcessedState() {
        return State.TRACK;
    }
}
