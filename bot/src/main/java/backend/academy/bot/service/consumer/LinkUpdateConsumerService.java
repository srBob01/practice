package backend.academy.bot.service.consumer;

import backend.academy.bot.service.notification.NotificationService;
import backend.academy.dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Сервис-подписчик для асинхронного приёма обновлений ссылок из Kafka.
 *
 * <p>Слушает топик, указанный в настройках приложения, десериализует сообщение в объект {@link LinkUpdate} и передаёт
 * его в {@link NotificationService} для обработки. После успешной обработки подтверждает приём сообщения методом
 * {@link Acknowledgment#acknowledge()}.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LinkUpdateConsumerService {

    private final NotificationService notificationService;

    /**
     * Метод-обработчик сообщений из Kafka.
     *
     * <p>Подписывается на топик, заданный в свойстве `${app.kafka.name}`. В случае получения сообщения вызывает
     * {@link NotificationService#handleUpdate(LinkUpdate)}
     *
     * @param update полученное обновление ссылки
     * @param ack объект для ручного подтверждения приёма сообщения
     */
    @KafkaListener(topics = "${app.kafka.name}", containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(LinkUpdate update, Acknowledgment ack) {
        log.info("Получено обновление для linkId={}", update.id());
        notificationService.handleUpdate(update);
        ack.acknowledge();
    }
}
