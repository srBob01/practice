package backend.academy.scrapper.checkupdate.handler;

import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;

/**
 * Интерфейс для обработчиков, отвечающих за получение деталей обновления для ссылок конкретного типа
 * ({@link LinkType}).
 *
 * @param <T> конкретный подкласс {@link Link}, который поддерживает этот хендлер
 */
public interface LinkUpdateHandler<T extends Link> {

    /**
     * Возвращает тип ссылок, которые обрабатывает данный хендлер.
     *
     * @return значение {@link LinkType}
     */
    LinkType getSupportedType();

    /**
     * Получает информацию о последнем обновлении для переданной ссылки.
     *
     * @param link сущность {@code T}, для которой запрашиваются детали обновления
     * @return объект {@link UpdateDetail} с данными о времени и характере обновления
     */
    UpdateDetail fetchUpdateDetail(T link);
}
