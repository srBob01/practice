package backend.academy.bot.state.impl.help;

import backend.academy.bot.converter.AddLinkRequestConverter;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.service.base.UserSessionService;
import backend.academy.bot.service.bot.TelegramBotSendService;
import backend.academy.bot.service.client.ScrapperClient;
import backend.academy.bot.service.formatter.LinkFormatter;
import backend.academy.dto.AddLinkRequest;
import java.util.ArrayList;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackingFinalizerService {
    private final ScrapperClient scrapperClient;
    private final AddLinkRequestConverter addLinkRequestConverter;
    private final TelegramBotSendService sendService;
    private final LinkFormatter linkFormatter;
    private final UserSessionService userSessionService;

    public void finalizeTracking(long chatId, UserSession session, Consumer<UserSession> nextStepCallback) {
        AddLinkRequest request = addLinkRequestConverter.convert(session);

        scrapperClient
                .addLink(chatId, request)
                .subscribe(
                        s -> {
                            nextStepCallback.accept(session);
                            clearSession(session);
                            userSessionService.save(chatId, session);
                            String msg = "Tracking info:\n" + linkFormatter.formatLink(s);
                            sendService.sendMessage(chatId, msg, session);
                        },
                        e -> sendService.sendMessage(chatId, "Failed to subscribe link. Please try later.", session));
    }

    private void clearSession(UserSession session) {
        session.link(null);
        session.tags(new ArrayList<>());
        session.filters(new ArrayList<>());
    }
}
