package backend.academy.bot.state.impl.next.track;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.service.validator.LinkValidationService;
import backend.academy.bot.state.impl.abst.AbstractStateHandlerWithNext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TWaitLinkStateHandler extends AbstractStateHandlerWithNext {
    private static final String ENTER_TAGS = "Link accepted. Please enter tags.";
    private static final String VALID_LINK_OR_TYPE_HELP = "Invalid link. Please enter a valid link.";
    private static final String ENTER_TEXT = "Please, enter text.";

    private final LinkValidationService linkValidationService;

    public TWaitLinkStateHandler(
            TelegramBotSendService sendService,
            StateMachine stateMachine,
            LinkValidationService linkValidationService,
            UserSessionService userSessionService) {
        super(sendService, stateMachine, userSessionService);
        this.linkValidationService = linkValidationService;
    }

    @Override
    public void handle(long chatId, String text) {
        log.info("Handling T_WAIT_LINK state for chatId={}, received text: {}", chatId, text);
        UserSession session = userSessionService.get(chatId);
        if (text == null) {
            sendService.sendMessage(chatId, ENTER_TEXT, session);
        } else {
            String link = text.trim();
            if (!link.isEmpty() && linkValidationService.validateLink(link)) {
                session.link(link);
                nextStep(session);
                userSessionService.save(chatId, session);
                sendService.sendMessage(chatId, ENTER_TAGS, userSessionService.get(chatId));
            } else {
                log.warn("Invalid link received in T_WAIT_LINK state for chatId={}", chatId);
                sendService.sendMessage(chatId, VALID_LINK_OR_TYPE_HELP, session);
            }
        }
    }

    @Override
    public State getProcessedState() {
        return State.T_WAIT_LINK;
    }
}
