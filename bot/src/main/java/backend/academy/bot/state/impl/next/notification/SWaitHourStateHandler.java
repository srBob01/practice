package backend.academy.bot.state.impl.next.notification;

import backend.academy.bot.entity.NotificationMode;
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
public class SWaitHourStateHandler extends AbstractStateHandlerWithNext {
    private static final String INVALID_HOUR_MESSAGE = "Invalid input. Enter the hour (0–23)";

    protected SWaitHourStateHandler(
            TelegramBotSendService sendService, StateMachine stateMachine, UserSessionService userSessionService) {
        super(sendService, stateMachine, userSessionService);
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling S_WAIT_HOUR for chatId={}, input='{}'", chatId, text);
        UserSession session = userSessionService.get(chatId);

        if (text == null || text.isBlank() || text.startsWith("/")) {
            sendService.sendMessage(chatId, INVALID_HOUR_MESSAGE, session);
            return;
        }

        int hour;
        try {
            hour = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            sendService.sendMessage(chatId, INVALID_HOUR_MESSAGE, session);
            return;
        }

        if (hour < 0 || hour > 23) {
            sendService.sendMessage(chatId, INVALID_HOUR_MESSAGE, session);
            return;
        }

        session.mode(NotificationMode.DAILY_DIGEST);
        session.digestHour(hour);

        nextStep(session);
        userSessionService.save(chatId, session);
        sendService.sendMessage(chatId, String.format("Daily digest hour set to *%d*:00*", hour), session);
    }

    @Override
    public State getProcessedState() {
        return State.S_WAIT_HOUR;
    }
}
