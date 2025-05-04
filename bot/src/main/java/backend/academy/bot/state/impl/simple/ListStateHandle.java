package backend.academy.bot.state.impl.simple;

import backend.academy.bot.entity.State;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.service.client.ScrapperClient;
import backend.academy.bot.service.formatter.LinkFormatter;
import backend.academy.bot.state.impl.abst.AbstractStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ListStateHandle extends AbstractStateHandler {
    private static final String NO_TRACKED_LINKS = "You have no tracked links.";
    private static final String ERROR_RETRIEVING = "Error retrieving your tracked links.";

    private final ScrapperClient scrapperClient;
    private final LinkFormatter linkFormatter;

    public ListStateHandle(
            TelegramBotSendService sendService,
            UserSessionService sessionService,
            ScrapperClient scrapperClient,
            LinkFormatter linkFormatter) {
        super(sendService, sessionService);
        this.scrapperClient = scrapperClient;
        this.linkFormatter = linkFormatter;
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling LIST state for chatId={}", chatId);
        scrapperClient
                .getTrackedLinks(chatId)
                .subscribe(
                        listLinksResponse -> sendService.sendMessage(
                                chatId,
                                listLinksResponse.links().isEmpty()
                                        ? NO_TRACKED_LINKS
                                        : linkFormatter.formatList(listLinksResponse),
                                userSessionService.get(chatId)),
                        e -> sendService.sendMessage(chatId, ERROR_RETRIEVING, userSessionService.get(chatId)));
    }

    @Override
    public State getProcessedState() {
        return State.LIST;
    }
}
