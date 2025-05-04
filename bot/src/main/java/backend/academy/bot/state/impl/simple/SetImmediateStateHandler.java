package backend.academy.bot.state.impl.simple;

import backend.academy.bot.entity.NotificationMode;
import backend.academy.bot.entity.State;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.service.notification.NotificationService;
import backend.academy.bot.state.impl.abst.AbstractStateHandler;
import org.springframework.stereotype.Component;

@Component
public class SetImmediateStateHandler extends AbstractStateHandler {

    private final NotificationService notificationService;

    public SetImmediateStateHandler(
            TelegramBotSendService sendService,
            UserSessionService userSessionService,
            NotificationService notificationService) {
        super(sendService, userSessionService);
        this.notificationService = notificationService;
    }

    @Override
    public void handle(long chatId, String text) {
        UserSession session = userSessionService.get(chatId);
        session.mode(NotificationMode.IMMEDIATE);
        userSessionService.save(chatId, session);

        // если были накопленные — отдадим их сейчас
        notificationService.flushDigest(chatId, session, "📬 Switching to IMMEDIATE. Pending messages:\n\n");

        sendService.sendMessage(
                chatId,
                "Notification mode set to *IMMEDIATE* (" + NotificationMode.IMMEDIATE.description() + ")",
                session);
    }

    @Override
    public State getProcessedState() {
        return State.SET_IMMEDIATE;
    }
}
