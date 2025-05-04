package backend.academy.scrapper.service.scheduler.dispatch.impl;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.checkupdate.main.LinkUpdater;
import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.outbox.OutboxMessage;
import backend.academy.scrapper.service.base.ChatLinkService;
import backend.academy.scrapper.service.base.LinkService;
import backend.academy.scrapper.service.base.OutboxService;
import backend.academy.scrapper.service.scheduler.dispatch.LinkDispatchHandler;
import backend.academy.scrapper.service.serialization.JsonSerializationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Обработчик обновлений ссылок через Kafka (Outbox-паттерн). */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.update.dispatcher-type", havingValue = "KAFKA", matchIfMissing = true)
@RequiredArgsConstructor
public class KafkaLinkDispatchHandler implements LinkDispatchHandler {
    private final LinkService linkService;
    private final ChatLinkService chatLinkService;
    private final LinkUpdater linkUpdater;
    private final OutboxService outboxService;
    private final JsonSerializationService jsonSer;
    private final String topic;

    /**
     * Обрабатывает одно обновление ссылки:
     *
     * <ol>
     *   <li>Получает детали обновления через {@link LinkUpdater}.
     *   <li>Если есть новое обновление, сохраняет поле <code>lastModified</code> и сериализует {@link LinkUpdate} в
     *       JSON, помещая в таблицу Outbox.
     *   <li>Реальная отправка из Outbox в Kafka производится отдельным сервисом.
     * </ol>
     *
     * @param link сущность ссылки для обработки
     */
    @Transactional
    @Override
    public void handleOne(Link link) {
        try {
            UpdateDetail detail = linkUpdater.fetchLastUpdate(link);
            LocalDateTime newTime = detail.getCreationTime();
            if (link.lastModified() == null || newTime.isAfter(link.lastModified())) {
                link.lastModified(newTime);
                linkService.updateLastModified(link);
                LinkUpdate dto = new LinkUpdate(
                        link.id(),
                        link.originalUrl(),
                        detail.getDescription(),
                        chatLinkService.getChatIdsByLinkId(link.id()));
                String json = jsonSer.toJson(dto);
                outboxService.save(new OutboxMessage(topic, json));
                log.info("Processed link {} via Kafka", link.id());
            }
        } catch (Exception e) {
            log.error("Error processing link {} via Kafka", link.id(), e);
        }
    }
}
