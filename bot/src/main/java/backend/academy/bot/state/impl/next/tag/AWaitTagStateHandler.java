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
import backend.academy.dto.TagLinkRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AWaitTagStateHandler extends AbstractStateHandlerWithNext {

    private static final String ENTER_TAG_MESSAGE =
            "Please enter link and tag in the format: link:tag. To exit input," + " enter /end.";
    private static final String INVALID_FORMAT =
            "Invalid format. Expected format: link:tag. To exit input," + " enter /end.";
    private static final String TAG_ADD_FAILED = "Failed to add tag. Please try again.";
    public static final String INVALID_LINK_MESSAGE =
            "The link is incorrect. Make sure that you have entered the" + " correct URL.";

    private final ScrapperClient scrapperClient;
    private final TagInputParser tagInputParser;
    private final LinkValidationService linkValidationService;

    public AWaitTagStateHandler(
            TelegramBotSendService sendService,
            StateMachine stateMachine,
            UserSessionService userSessionService,
            ScrapperClient scrapperClient,
            TagInputParser tagInputParser,
            LinkValidationService linkValidationService) {
        super(sendService, stateMachine, userSessionService);
        this.scrapperClient = scrapperClient;
        this.tagInputParser = tagInputParser;
        this.linkValidationService = linkValidationService;
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling A_WAIT_TAG for chatId={}, input: {}", chatId, text);
        UserSession session = userSessionService.get(chatId);

        if (text == null || text.isBlank() || text.startsWith("/")) {
            sendService.sendMessage(chatId, ENTER_TAG_MESSAGE, session);
            return;
        }

        tagInputParser
                .parse(text)
                .ifPresentOrElse(
                        pair -> {
                            if (!linkValidationService.validateLink(pair.link())) {
                                sendService.sendMessage(chatId, INVALID_LINK_MESSAGE, session);
                                return;
                            }
                            TagLinkRequest request = new TagLinkRequest(chatId, pair.link(), pair.tag());
                            scrapperClient
                                    .addTag(request)
                                    .subscribe(
                                            success -> {
                                                nextStep(session);
                                                userSessionService.save(chatId, session);
                                                sendService.sendMessage(
                                                        chatId,
                                                        "Tag '" + pair.tag()
                                                                + "' has been successfully added to the link:\n"
                                                                + pair.link(),
                                                        session);
                                            },
                                            error -> {
                                                log.error(
                                                        "Error adding tag for chat {}: {}", chatId, error.getMessage());
                                                sendService.sendMessage(chatId, TAG_ADD_FAILED, session);
                                            });
                        },
                        () -> sendService.sendMessage(chatId, INVALID_FORMAT, session));
    }

    @Override
    public State getProcessedState() {
        return State.A_WAIT_TAG;
    }
}
