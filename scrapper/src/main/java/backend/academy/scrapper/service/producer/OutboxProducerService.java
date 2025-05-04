package backend.academy.scrapper.service.producer;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.service.base.OutboxService;
import backend.academy.scrapper.service.serialization.JsonSerializationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Сервис-публикатор для отправки сообщений из таблицы Outbox в Kafka.
 *
 * <p>Периодически запрашивает необработанные записи из Outbox через {@link OutboxService}, десериализует полезную
 * нагрузку в {@link LinkUpdate} и публикует в указанный топик при помощи {@link KafkaTemplate}. После успешной отправки
 * сообщение помечается как обработанное, в противном случае логируется ошибка.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxProducerService {
    private final OutboxService outboxService;
    private final KafkaTemplate<Long, LinkUpdate> kafka;
    private final ScrapperConfig cfg;
    private final JsonSerializationService jsonSer;

    /**
     * Запускается по расписанию (fixed delay) и отправляет в Kafka неотправленные сообщения из Outbox.
     *
     * <p>Размер пакета берётся из конфигурации ({@code cfg.update().batchLimit()}).
     */
    @Scheduled(fixedDelayString = "${app.kafka.poll-interval-ms}")
    public void publishUnprocessed() {
        var batch = outboxService.findUnprocessed(cfg.update().batchLimit());
        for (var msg : batch) {
            try {
                LinkUpdate record = jsonSer.fromJson(msg.payload(), LinkUpdate.class);

                var unused = kafka.send(msg.topic(), record.id(), record).whenComplete((result, ex) -> {
                    if (ex == null) {
                        outboxService.markProcessed(msg.id());
                        log.info("Outbox {} published marked processed", msg.id());
                    } else {
                        log.error("Kafka send failed for outbox {}", msg.id(), ex);
                    }
                });

            } catch (Exception e) {
                log.error("Failed to process outbox {}", msg.id(), e);
            }
        }
    }
}
