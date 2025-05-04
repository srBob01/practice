package backend.academy.scrapper.service.scheduler.dispatch;

import backend.academy.scrapper.model.db.link.Link;

/** Интерфейс для обработки обновлений ссылок. Обновить lastModified и отправить уведомление (Kafka или HTTP). */
public interface LinkDispatchHandler {

    /**
     * Обработать одну ссылку: проверить на новую версию, обновить <code>lastModified</code> и отправить уведомление.
     *
     * @param link сущность ссылки для обработки
     */
    void handleOne(Link link);
}
