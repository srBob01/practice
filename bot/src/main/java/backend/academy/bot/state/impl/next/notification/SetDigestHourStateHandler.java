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
public class SetDigestHourStateHandler extends AbstractStateHandlerWithNext {

    private static final String PROMPT_HOUR =
            "Please enter the hour (0–23) when you want to receive your daily digest:";

    private static final String INCORRECT_TYPE =
            "To perform the action, switch to the DAILY_DIGEST mode using /set_daily_digest";

    public SetDigestHourStateHandler(
            TelegramBotSendService sendService, StateMachine stateMachine, UserSessionService userSessionService) {
        super(sendService, stateMachine, userSessionService);
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling SET_DAILY_DIGEST state for chatId={}", chatId);
        UserSession session = userSessionService.get(chatId);
        if (session.mode() != NotificationMode.DAILY_DIGEST) {
            session.state(State.HELP);
            userSessionService.save(chatId, session);
            sendService.sendMessage(chatId, INCORRECT_TYPE, session);
        } else {
            nextStep(session);
            userSessionService.save(chatId, session);
            sendService.sendMessage(chatId, PROMPT_HOUR, session);
        }
    }

    @Override
    public State getProcessedState() {
        return State.SET_DIGEST_HOUR;
    }
}
