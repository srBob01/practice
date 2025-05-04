package backend.academy.scrapper.service.scheduler;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.service.base.LinkService;
import backend.academy.scrapper.service.scheduler.processor.LinkUpdateProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Планировщик, который каждые {@code app.update.interval-seconds} секунд запрашивает из {@link LinkService} пакет
 * ссылок на обновление (до {@code app.update.batch-limit} штук) и передаёт их на обработку в текущую стратегию
 * {@link LinkUpdateProcessor}.
 *
 * <p>Интервал опроса фиксирован константой {@code DELAY_MS}, остальные параметры (batch‑limit, interval‑seconds, режим
 * обработки) настраиваются через {@link ScrapperConfig.Update}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdateScheduler {
    private final int intervalSeconds;
    private final int batchLimit;
    private final LinkService linkService;
    private final LinkUpdateProcessor updateProcessor;

    /**
     * Выполняет один цикл проверки: получает список ссылок, которые давно не проверялись, логирует результат,
     * делегирует обработку {@link LinkUpdateProcessor}, и по её окончании логирует завершение.
     */
    @Scheduled(fixedDelayString = "${app.update.delay-ms}")
    public void checkForUpdates() {
        List<Link> batch = linkService.fetchBatchToUpdate(intervalSeconds, batchLimit);
        if (batch.isEmpty()) {
            log.info("No links to update in this batch");
            return;
        }
        updateProcessor.process(batch);
        log.info("Batch update processing completed");
    }
}
