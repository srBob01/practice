package backend.academy.bot.state.impl.next.tag;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.service.client.ScrapperClient;
import backend.academy.bot.service.formatter.LinkFormatter;
import backend.academy.bot.state.impl.abst.AbstractStateHandlerWithNext;
import backend.academy.dto.TagRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LWaitTagStateHandler extends AbstractStateHandlerWithNext {

    private static final String ENTER_TAG_MESSAGE =
            "Please enter the tag name to view associated links. To exit input, enter /end.";
    private static final String TAG_NOT_FOUND = "No links found for the provided tag.";
    private static final String FETCH_ERROR = "Error retrieving links for the tag.";

    private final ScrapperClient scrapperClient;
    private final LinkFormatter linkFormatter;

    public LWaitTagStateHandler(
            TelegramBotSendService sendService,
            UserSessionService userSessionService,
            StateMachine stateMachine,
            ScrapperClient scrapperClient,
            LinkFormatter linkFormatter) {
        super(sendService, stateMachine, userSessionService);
        this.scrapperClient = scrapperClient;
        this.linkFormatter = linkFormatter;
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling L_WAIT_TAG for chatId={}, input: {}", chatId, text);
        UserSession session = userSessionService.get(chatId);

        if (text == null || text.isBlank() || text.startsWith("/")) {
            sendService.sendMessage(chatId, ENTER_TAG_MESSAGE, session);
            return;
        }

        TagRequest request = new TagRequest(chatId, text.trim());

        scrapperClient
                .getLinksByTag(request)
                .subscribe(
                        response -> {
                            nextStep(session);
                            userSessionService.save(chatId, session);
                            if (response.links().isEmpty()) {
                                sendService.sendMessage(chatId, TAG_NOT_FOUND, session);
                            } else {
                                sendService.sendMessage(
                                        chatId, linkFormatter.formatListForTag(text.trim(), response.links()), session);
                            }
                        },
                        error -> {
                            log.error("Error retrieving links by tag for chat {}: {}", chatId, error.getMessage());
                            sendService.sendMessage(chatId, FETCH_ERROR, session);
                        });
    }

    @Override
    public State getProcessedState() {
        return State.L_WAIT_TAG;
    }
}
