package backend.academy.scrapper.repository.jdbc.link.impl;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import java.util.List;
import java.util.Map;

/**
 * Репозиторий для операций вставки и массовой выборки деталей для конкретного подкласса {@link Link}.
 *
 * @param <T> конкретный тип Link
 */
public interface LinkImplRepository<T extends Link> {

    /**
     * Вставляет новую запись типа T в базу.
     *
     * @param link экземпляр T для сохранения
     */
    void insert(T link);

    /**
     * Массово загружает детали по списку идентификаторов и заполняет их в переданную map: ключ = id, значение =
     * экземпляр T для заполнения.
     *
     * @param ids список идентификаторов сущностей
     * @param map отображение id→объект, который нужно обогатить
     */
    void findByIds(List<Long> ids, Map<Long, T> map);

    /**
     * Тип ссылок, поддерживаемый этим репозиторием.
     *
     * @return {@link LinkType}, для которого предназначены insert и findByIds
     */
    LinkType getSupportedType();
}
