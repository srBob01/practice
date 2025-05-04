package backend.academy.bot.state.impl.next.track;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.state.impl.abst.AbstractStateHandlerWithNext;
import backend.academy.bot.state.impl.help.TrackingFinalizerService;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TWaitFiltersStateHandle extends AbstractStateHandlerWithNext {
    private static final String FILTERS_SAVED = "Filters saved. Now finalizing your tracking";
    private static final String ENTER_TEXT = "Please enter filters in the correct format (e.g., work hobby).";

    private final TrackingFinalizerService trackingFinalizerService;

    public TWaitFiltersStateHandle(
            TelegramBotSendService sendService,
            StateMachine stateMachine,
            UserSessionService userSessionService,
            TrackingFinalizerService trackingFinalizerService) {
        super(sendService, stateMachine, userSessionService);
        this.trackingFinalizerService = trackingFinalizerService;
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling T_WAIT_FILTERS state for chatId={}, received text: {}", chatId, text);
        UserSession session = userSessionService.get(chatId);

        if (text == null || text.isEmpty() || text.isBlank() || text.startsWith("/")) {
            log.warn("Unexpected input in T_WAIT_FILTERS state for chatId={}", chatId);
            sendService.sendMessage(chatId, ENTER_TEXT, session);
        } else {
            List<String> filters = Arrays.asList(text.split("\\s+"));
            session.filters(filters);
            userSessionService.save(chatId, session);
            sendService.sendMessage(chatId, FILTERS_SAVED, session);
            trackingFinalizerService.finalizeTracking(chatId, session, this::nextStep);
        }
    }

    @Override
    public State getProcessedState() {
        return State.T_WAIT_FILTERS;
    }
}
