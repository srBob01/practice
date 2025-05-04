package backend.academy.bot.state.impl.simple;

import backend.academy.bot.entity.State;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.state.impl.abst.AbstractStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IncorrectInputHandle extends AbstractStateHandler {

    private static final String TRY_HELP = "Incorrect input. Try /help";

    public IncorrectInputHandle(TelegramBotSendService sendService, UserSessionService sessionService) {
        super(sendService, sessionService);
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling INCORRECT_INPUT state for chatId={}", chatId);
        sendService.sendMessage(chatId, TRY_HELP, userSessionService.get(chatId));
    }

    @Override
    public State getProcessedState() {
        return State.INCORRECT_INPUT;
    }
}
