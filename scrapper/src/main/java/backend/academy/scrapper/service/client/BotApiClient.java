package backend.academy.scrapper.service.client;

import backend.academy.dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Клиент для отправки уведомлений об обновлениях ссылок в бота. Обёртывает декларативный интерфейс
 * {@link BotDeclarativeClient} для добавления логирования и обработки ошибок.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BotApiClient {

    private final BotDeclarativeClient client;

    public Mono<Void> sendUpdate(LinkUpdate updateDto) {
        return client.sendUpdate(updateDto)
                .doOnSuccess(s -> log.info("Successfully sent update: {}", updateDto))
                .doOnError(e -> log.error("Error sending update for {}", updateDto.url()))
                .then();
    }
}
