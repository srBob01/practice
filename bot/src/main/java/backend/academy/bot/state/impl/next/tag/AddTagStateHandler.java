package backend.academy.bot.state.impl.next.tag;

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
public class AddTagStateHandler extends AbstractStateHandlerWithNext {

    private static final String ENTER_TAG_MESSAGE =
            "Please enter link and tag in the format: link:tag. To exit input, enter /end.";

    public AddTagStateHandler(
            TelegramBotSendService sendService, StateMachine stateMachine, UserSessionService userSessionService) {
        super(sendService, stateMachine, userSessionService);
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling ADD_TAG state for chatId={}", chatId);
        UserSession session = userSessionService.get(chatId);
        nextStep(session);
        userSessionService.save(chatId, session);
        sendService.sendMessage(chatId, ENTER_TAG_MESSAGE, session);
    }

    @Override
    public State getProcessedState() {
        return State.ADD_TAG;
    }
}
