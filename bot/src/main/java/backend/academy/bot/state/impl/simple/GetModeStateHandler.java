package backend.academy.bot.state.impl.simple;

import backend.academy.bot.entity.NotificationMode;
import backend.academy.bot.entity.State;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.impl.RedisUserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.state.impl.abst.AbstractStateHandler;
import org.springframework.stereotype.Component;

@Component
public class GetModeStateHandler extends AbstractStateHandler {
    protected GetModeStateHandler(TelegramBotSendService sendService, RedisUserSessionService userSessionService) {
        super(sendService, userSessionService);
    }

    @Override
    public void handle(long chatId, String text) {
        UserSession session = userSessionService.get(chatId);
        NotificationMode mode = session.mode();

        StringBuilder sb = new StringBuilder();
        sb.append("Current notification mode:\n");
        sb.append("*").append(mode.name()).append("*\n");
        sb.append("(").append(mode.description()).append(")");

        if (mode == NotificationMode.DAILY_DIGEST) {
            int hour = session.digestHour();
            sb.append("\nScheduled at: *").append(hour).append(":00*");
        }

        sendService.sendMessage(chatId, sb.toString(), session);
    }

    @Override
    public State getProcessedState() {
        return State.GET_MODE;
    }
}
