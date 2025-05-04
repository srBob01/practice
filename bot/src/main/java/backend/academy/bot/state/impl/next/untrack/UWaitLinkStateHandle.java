package backend.academy.bot.state.impl.next.untrack;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.service.client.ScrapperClient;
import backend.academy.bot.service.validator.LinkValidationService;
import backend.academy.bot.state.impl.abst.AbstractStateHandlerWithNext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UWaitLinkStateHandle extends AbstractStateHandlerWithNext {
    private static final String REMOVED = "Link removed: ";
    private static final String FAILED = "Failed to remove link. Possibly it doesn't exist.";
    private static final String VALID_LINK = "Input valid link. Try again.";
    private static final String ENTER_TEXT = "Please, enter text.";

    private final LinkValidationService linkValidationService;
    private final ScrapperClient scrapperClient;
    private final Integer simplePriorityCost;

    public UWaitLinkStateHandle(
            ScrapperClient scrapperClient,
            TelegramBotSendService sendService,
            StateMachine stateMachine,
            UserSessionService userSessionService,
            LinkValidationService linkValidationService,
            @Qualifier("simpleCostPriority") Integer simplePriorityCost) {
        super(sendService, stateMachine, userSessionService);
        this.scrapperClient = scrapperClient;
        this.linkValidationService = linkValidationService;
        this.simplePriorityCost = simplePriorityCost;
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling U_WAIT_LINK state for chatId={}, received text: {}", chatId, text);
        UserSession session = userSessionService.get(chatId);
        if (text == null) {
            sendService.sendMessage(chatId, ENTER_TEXT, session);
        } else if (linkValidationService.validateLink(text, simplePriorityCost)) {
            scrapperClient
                    .deleteLink(chatId, text)
                    .doOnSuccess(__ -> {
                        nextStep(session);
                        userSessionService.save(chatId, session);
                        sendService.sendMessage(chatId, REMOVED + text, session);
                    })
                    .subscribe(__ -> {}, e -> {
                        log.error("Failed to remove link {} for chat {}: {}", text, chatId, e.getMessage());
                        sendService.sendMessage(chatId, FAILED, session);
                    });

        } else {
            sendService.sendMessage(chatId, VALID_LINK, session);
        }
    }

    @Override
    public State getProcessedState() {
        return State.U_WAIT_LINK;
    }
}
