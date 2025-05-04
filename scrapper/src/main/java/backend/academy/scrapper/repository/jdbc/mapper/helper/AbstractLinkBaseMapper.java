package backend.academy.scrapper.repository.jdbc.mapper.helper;

import backend.academy.scrapper.model.db.link.Link;
import java.time.LocalDateTime;

/**
 * Базовая абстракция {@link LinkBaseMapper}, которая:
 *
 * <ul>
 *   <li>Самостоятельно заполняет общие поля через {@link #mapBase}.
 *   <li>Оставляет вызов {@link #createEmptyInstance()} для подклассов.
 * </ul>
 *
 * @param <T> конкретный тип {@link Link}
 */
public abstract class AbstractLinkBaseMapper<T extends Link> implements LinkBaseMapper<T> {
    /**
     * Заполняет у нового объекта T поля id, url, lastModified и version. Вызывает {@link #createEmptyInstance()} для
     * создания пустого экземпляра.
     *
     * @param id идентификатор записи
     * @param url оригинальный URL
     * @param lastModified время последнего изменения
     * @param version версия для оптимистической блокировки
     * @return объект T с заполненными данными
     */
    @Override
    public T mapBase(Long id, String url, LocalDateTime lastModified, Long version) {
        T link = createEmptyInstance();
        link.id(id);
        link.originalUrl(url);
        link.lastModified(lastModified);
        link.version(version);
        return link;
    }

    /**
     * Создаёт «пустой» экземпляр T, чтобы в него можно было затем установить общие поля в {@link #mapBase}.
     *
     * @return новый объект T без предварительно заполненных полей
     */
    protected abstract T createEmptyInstance();
}
