package backend.academy.bot.state.impl.next.tag;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.service.client.ScrapperClient;
import backend.academy.bot.state.impl.abst.AbstractStateHandlerWithNext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DLWaitTagStateHandler extends AbstractStateHandlerWithNext {

    private static final String ENTER_TAG_MESSAGE =
            "Please enter the tag name to delete all associated links. To exit input, enter /end.";
    private static final String DELETE_SUCCESS = "All links with the specified tag have been successfully deleted.";
    private static final String DELETE_ERROR = "Failed to delete links with the specified tag. Please try again later.";

    private final ScrapperClient scrapperClient;

    public DLWaitTagStateHandler(
            TelegramBotSendService sendService,
            UserSessionService userSessionService,
            StateMachine stateMachine,
            ScrapperClient scrapperClient) {
        super(sendService, stateMachine, userSessionService);
        this.scrapperClient = scrapperClient;
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling DL_WAIT_TAG for chatId={}, input: {}", chatId, text);
        UserSession session = userSessionService.get(chatId);

        if (text == null || text.isBlank() || text.startsWith("/")) {
            sendService.sendMessage(chatId, ENTER_TAG_MESSAGE, session);
            return;
        }

        String tag = text.trim();

        scrapperClient
                .deleteLinksByTag(chatId, tag)
                .doOnSuccess(__ -> {
                    nextStep(session);
                    userSessionService.save(chatId, session);
                    sendService.sendMessage(chatId, DELETE_SUCCESS, session);
                })
                .subscribe(__ -> {}, e -> {
                    log.error("Error deleting links by tag '{}' for chat {}: {}", tag, chatId, e.getMessage());
                    sendService.sendMessage(chatId, DELETE_ERROR, session);
                });
    }

    @Override
    public State getProcessedState() {
        return State.DL_WAIT_TAG;
    }
}
