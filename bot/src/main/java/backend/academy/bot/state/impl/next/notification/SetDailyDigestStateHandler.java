package backend.academy.bot.state.impl.next.notification;

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
public class SetDailyDigestStateHandler extends AbstractStateHandlerWithNext {

    private static final String PROMPT_HOUR =
            "Please enter the hour (0–23) when you want to receive your daily digest:";

    public SetDailyDigestStateHandler(
            TelegramBotSendService sendService, StateMachine stateMachine, UserSessionService userSessionService) {
        super(sendService, stateMachine, userSessionService);
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling SET_DAILY_DIGEST state for chatId={}", chatId);
        UserSession session = userSessionService.get(chatId);
        nextStep(session);
        userSessionService.save(chatId, session);
        sendService.sendMessage(chatId, PROMPT_HOUR, session);
    }

    @Override
    public State getProcessedState() {
        return State.SET_DAILY_DIGEST;
    }
}
