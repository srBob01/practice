package backend.academy.bot.state.impl.simple;

import backend.academy.bot.converter.ChatRequestConverter;
import backend.academy.bot.entity.State;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.service.client.ScrapperClient;
import backend.academy.bot.state.impl.abst.AbstractStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartStateHandler extends AbstractStateHandler {

    private static final String WELCOME = "Welcome to the bot! Type /help to see available commands.";
    private static final String REGISTRATION_ERROR = "You already registered.";

    private final ScrapperClient scrapperClient;
    private final ChatRequestConverter converter;

    public StartStateHandler(
            ScrapperClient scrapperClient,
            TelegramBotSendService sendService,
            UserSessionService sessionService,
            ChatRequestConverter converter) {
        super(sendService, sessionService);
        this.scrapperClient = scrapperClient;
        this.converter = converter;
    }

    @Override
    public void handle(long chatId, String text) {
        scrapperClient
                .registerChat(converter.convert(chatId))
                .subscribe(
                        chatResponse -> {
                            userSessionService.createSession(chatResponse.id());
                            sendService.sendMessage(chatResponse.id(), WELCOME, userSessionService.get(chatId));
                        },
                        e -> {
                            log.error("Ошибка при регистрации чата {}: {}", chatId, e.getMessage());
                            sendService.sendMessage(chatId, REGISTRATION_ERROR, userSessionService.get(chatId));
                        });
    }

    @Override
    public State getProcessedState() {
        return State.START;
    }
}
