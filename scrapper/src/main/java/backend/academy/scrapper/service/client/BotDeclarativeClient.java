package backend.academy.scrapper.service.client;

import backend.academy.dto.LinkUpdate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange
public interface BotDeclarativeClient {

    /**
     * Отправляет обновление об изменении ссылки.
     *
     * @param updateDto DTO с данными обновления ссылки.
     * @return Mono<Void> сигнал завершения операции.
     */
    @PostExchange(url = "/updates")
    Mono<String> sendUpdate(@RequestBody LinkUpdate updateDto);
}
