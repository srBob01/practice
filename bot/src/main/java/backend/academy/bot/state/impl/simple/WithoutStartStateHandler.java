package backend.academy.bot.state.impl.simple;

import backend.academy.bot.entity.State;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.state.impl.abst.AbstractStateHandler;
import org.springframework.stereotype.Component;

@Component
public class WithoutStartStateHandler extends AbstractStateHandler {

    private static final String START = "To start program enter /start";

    public WithoutStartStateHandler(TelegramBotSendService sendService, UserSessionService userSessionService) {
        super(sendService, userSessionService);
    }

    @Override
    public void handle(long chatId, String text) {
        sendService.sendMessage(chatId, START, null);
    }

    @Override
    public State getProcessedState() {
        return State.WITHOUT_START;
    }
}
