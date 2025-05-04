package backend.academy.scrapper.service.scheduler.processor;

import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.Link;
import java.util.List;

/**
 * Стратегия обработки пачки ссылок.
 *
 * <p>Реализации могут выполнять последовательную или параллельную обработку.
 */
public interface LinkUpdateProcessor {
    /**
     * Обрабатывает переданный список ссылок: для каждой ссылки получает {@link UpdateDetail}, сравнивает время
     * предыдущего обновления со свежим, и при необходимости сохраняет новое время и отправляет уведомление.
     *
     * @param links список ссылок для обработки
     */
    void process(List<Link> links);
}
