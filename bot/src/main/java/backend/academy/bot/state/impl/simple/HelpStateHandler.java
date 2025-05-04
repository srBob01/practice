package backend.academy.bot.state.impl.simple;

import backend.academy.bot.entity.State;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.state.impl.abst.AbstractStateHandler;
import org.springframework.stereotype.Component;

@Component
public class HelpStateHandler extends AbstractStateHandler {

    private static final String COMMANDS =
            """
        Available commands:
        /help - show this command list
        /unregister - unregister and delete user data
        /track - track a new link
        /untrack - stop tracking a link
        /list - show all tracked links
        /add_tag - add tag to a link
        /delete_tag - delete tag from a link
        /links_by_tag - show links by tag
        /delete_links_by_tag - delete all links with a specific tag
        /get_mode - show current notification mode
        /set_immediate - switch to immediate notifications
        /set_daily_digest - switch to daily digest mode
        /set_digest_hour - set hour for daily digest
        """;

    public HelpStateHandler(TelegramBotSendService sendService, UserSessionService sessionService) {
        super(sendService, sessionService);
    }

    @Override
    public void handle(long chatId, String text) {
        sendService.sendMessage(chatId, COMMANDS, userSessionService.get(chatId));
    }

    @Override
    public State getProcessedState() {
        return State.HELP;
    }
}
