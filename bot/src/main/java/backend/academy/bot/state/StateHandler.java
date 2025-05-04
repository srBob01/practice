package backend.academy.bot.state;

import backend.academy.bot.entity.State;

/**
 * Интерфейс обработчика состояния для Telegram-бота. Определяет методы для обработки входящих сообщений и получения
 * обрабатываемого состояния.
 */
public interface StateHandler {
    /**
     * Обрабатывает входящее сообщение для указанного chatId.
     *
     * @param chatId идентификатор чата
     * @param text текст сообщения
     */
    void handle(long chatId, String text);

    /**
     * Возвращает состояние, которое обрабатывается данным обработчиком.
     *
     * @return обрабатываемое состояние
     */
    State getProcessedState();
}
