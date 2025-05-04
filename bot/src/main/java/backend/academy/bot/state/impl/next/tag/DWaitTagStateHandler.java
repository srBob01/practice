package backend.academy.bot.state.impl.next.tag;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.service.client.ScrapperClient;
import backend.academy.bot.service.validator.LinkValidationService;
import backend.academy.bot.state.impl.abst.AbstractStateHandlerWithNext;
import backend.academy.bot.state.impl.help.TagInputParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DWaitTagStateHandler extends AbstractStateHandlerWithNext {

    private static final String ENTER_TAG_MESSAGE =
            "Please enter link and tag to delete in the format: link:tag. " + "To exit input, enter /end.";
    private static final String INVALID_FORMAT =
            "Invalid format. Expected format: link:tag. To exit input," + " enter /end.";
    private static final String TAG_DELETE_FAILED = "Failed to delete tag. Please try again later.";
    public static final String INVALID_LINK_MESSAGE =
            "The link is incorrect. Make sure that you have entered the " + "correct URL.";

    private final ScrapperClient scrapperClient;
    private final TagInputParser tagInputParser;
    private final LinkValidationService linkValidationService;
    private final Integer simpleCostPriority;

    public DWaitTagStateHandler(
            TelegramBotSendService sendService,
            UserSessionService userSessionService,
            StateMachine stateMachine,
            ScrapperClient scrapperClient,
            TagInputParser tagInputParser,
            LinkValidationService linkValidationService,
            Integer simpleCostPriority) {
        super(sendService, stateMachine, userSessionService);
        this.scrapperClient = scrapperClient;
        this.tagInputParser = tagInputParser;
        this.linkValidationService = linkValidationService;
        this.simpleCostPriority = simpleCostPriority;
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling D_WAIT_TAG for chatId={}, input: {}", chatId, text);
        UserSession session = userSessionService.get(chatId);

        if (text == null || text.isBlank() || text.startsWith("/")) {
            sendService.sendMessage(chatId, ENTER_TAG_MESSAGE, session);
            return;
        }

        tagInputParser
                .parse(text)
                .ifPresentOrElse(
                        pair -> {
                            if (!linkValidationService.validateLink(pair.link(), simpleCostPriority)) {
                                sendService.sendMessage(chatId, INVALID_LINK_MESSAGE, session);
                                return;
                            }

                            String link = pair.link();
                            String tag = pair.tag();

                            scrapperClient
                                    .deleteTag(chatId, link, tag)
                                    .doOnSuccess(__ -> {
                                        nextStep(session);
                                        userSessionService.save(chatId, session);
                                        sendService.sendMessage(
                                                chatId,
                                                "Tag '" + tag
                                                        + "' has been successfully removed from the link:\n"
                                                        + link,
                                                session);
                                    })
                                    .subscribe(__ -> {}, e -> {
                                        log.error(
                                                "Error deleting tag '{}' for link '{}' and chat {}: {}",
                                                tag,
                                                link,
                                                chatId,
                                                e.getMessage());
                                        sendService.sendMessage(chatId, TAG_DELETE_FAILED, session);
                                    });
                        },
                        () -> sendService.sendMessage(chatId, INVALID_FORMAT, session));
    }

    @Override
    public State getProcessedState() {
        return State.D_WAIT_TAG;
    }
}
