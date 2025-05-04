package backend.academy.scrapper.service.base;

import backend.academy.scrapper.model.db.outbox.OutboxMessage;
import java.util.List;

/**
 * Сервис для работы с Outbox-паттерном асинхронной отправки сообщений.
 *
 * <p>Предоставляет методы сохранения нового сообщения в outbox, выборки необработанных сообщений и маркировки их как
 * обработанных после успешной передачи.
 */
public interface OutboxService {

    /**
     * Сохраняет новое сообщение в таблицу outbox.
     *
     * @param message объект {@link OutboxMessage} для сохранения
     * @return сохранённый объект с заполненным идентификатором и метаданными
     */
    OutboxMessage save(OutboxMessage message);

    /**
     * Находит необработанные (неотправленные) сообщения.
     *
     * @param limit максимальное количество сообщений для выборки
     * @return список сообщений, ожидающих обработки
     */
    List<OutboxMessage> findUnprocessed(int limit);

    /**
     * Помечает сообщение как обработанное после успешной отправки.
     *
     * @param id идентификатор сообщения в таблице outbox
     */
    void markProcessed(Long id);
}
