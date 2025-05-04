package backend.academy.scrapper.service.scheduler.dispatch.impl;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.checkupdate.main.LinkUpdater;
import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.service.base.ChatLinkService;
import backend.academy.scrapper.service.base.LinkService;
import backend.academy.scrapper.service.client.BotApiClient;
import backend.academy.scrapper.service.scheduler.dispatch.LinkDispatchHandler;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Обработчик обновлений ссылок через HTTP.
 *
 * <p>Скачивает детали обновления по ссылке при помощи {@link LinkUpdater}, обновляет поле lastModified в базе и
 * посылает уведомление в Scrapper API через {@link BotApiClient}. Адаптивно пропускает обновления, если изменений нет.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.update.dispatcher-type", havingValue = "HTTP")
@RequiredArgsConstructor
public class HttpLinkDispatchHandler implements LinkDispatchHandler {
    private final LinkService linkService;
    private final ChatLinkService chatLinkService;
    private final LinkUpdater linkUpdater;
    private final BotApiClient botApiClient;

    /**
     * Обрабатывает одно обновление ссылки:
     *
     * <ol>
     *   <li>Получает детали обновления через {@link LinkUpdater}.
     *   <li>Если есть новое обновление, сохраняет поле <code>lastModified</code> и отправляет {@link LinkUpdate} в
     *       Scrapper API.
     *   <li>Логи успешной обработки и ошибки записываются через {@link org.slf4j.Logger}.
     * </ol>
     *
     * @param link сущность ссылки для обработки
     */
    @Override
    public void handleOne(Link link) {
        try {
            UpdateDetail detail = linkUpdater.fetchLastUpdate(link);
            LocalDateTime newTime = detail.getCreationTime();
            if (link.lastModified() == null || newTime.isAfter(link.lastModified())) {
                link.lastModified(newTime);
                linkService.updateLastModified(link);

                List<Long> chatIds = chatLinkService.getChatIdsByLinkId(link.id());
                LinkUpdate dto = new LinkUpdate(link.id(), link.originalUrl(), detail.getDescription(), chatIds);

                botApiClient
                        .sendUpdate(dto)
                        .doOnSuccess(__ -> log.info("Sent HTTP update for link {}", link.id()))
                        .doOnError(err ->
                                log.error("Error sending HTTP update for link {}: {}", link.id(), err.getMessage()))
                        .subscribe();

                log.info("Processed link {} via HTTP", link.id());
            }
        } catch (Exception e) {
            log.error("Error processing link {} via HTTP", link.id(), e);
        }
    }
}
