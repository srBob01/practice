package backend.academy.bot.state.impl.next.track;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.state.impl.abst.AbstractStateHandlerWithNext;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TWaitTagStateHandle extends AbstractStateHandlerWithNext {

    private static final String ENTER_FILTERS = "Tags saved. Now enter filters.";
    private static final String ENTER_TAGS = "Please enter tags in the correct format (e.g., work hobby).";

    public TWaitTagStateHandle(
            TelegramBotSendService sendService, StateMachine stateMachine, UserSessionService userSessionService) {
        super(sendService, stateMachine, userSessionService);
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling T_WAIT_TAG state for chatId={}, received text: {}", chatId, text);

        UserSession session = userSessionService.get(chatId);

        if (text == null || text.isEmpty() || text.isBlank() || text.startsWith("/")) {
            sendService.sendMessage(chatId, ENTER_TAGS, session);
        } else {
            List<String> tags = Arrays.asList(text.split("\\s+"));
            session.tags(tags);
            nextStep(session);
            userSessionService.save(chatId, session);
            sendService.sendMessage(chatId, ENTER_FILTERS, session);
        }
    }

    @Override
    public State getProcessedState() {
        return State.T_WAIT_TAG;
    }
}
