package backend.academy.bot.state.impl.simple;

import backend.academy.bot.entity.State;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.service.client.ScrapperClient;
import backend.academy.bot.state.impl.abst.AbstractStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UnregisterStateHandler extends AbstractStateHandler {

    private static final String SUCCESS = "You have been successfully removed from the system!";
    private static final String FAILURE = "You were not registered, or an error occurred.";

    private final ScrapperClient scrapperClient;

    public UnregisterStateHandler(
            ScrapperClient scrapperClient, TelegramBotSendService sendService, UserSessionService sessionService) {
        super(sendService, sessionService);
        this.scrapperClient = scrapperClient;
    }

    @Override
    public void handle(long chatId, String text) {
        scrapperClient
                .deleteChat(chatId)
                .doOnSuccess(__ -> {
                    userSessionService.remove(chatId);
                    sendService.sendMessage(chatId, SUCCESS, null);
                })
                .subscribe(__ -> {}, e -> {
                    log.warn("Ошибка при удалении чата {}", chatId, e);
                    sendService.sendMessage(chatId, FAILURE, userSessionService.get(chatId));
                });
    }

    @Override
    public State getProcessedState() {
        return State.UNREGISTER;
    }
}
