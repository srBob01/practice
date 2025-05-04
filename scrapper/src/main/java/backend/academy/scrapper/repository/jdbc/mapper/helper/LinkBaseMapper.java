package backend.academy.scrapper.repository.jdbc.mapper.helper;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import java.time.LocalDateTime;

/**
 * Интерфейс для «базовых» мапперов ссылок, которые умеют:
 *
 * <ul>
 *   <li>Обозначить, какой {@link LinkType} они поддерживают.
 *   <li>Заполнить общие поля {@code id}, {@code url}, {@code lastModified}, {@code version}.
 * </ul>
 *
 * @param <T> конкретный подкласс {@link Link}
 */
public interface LinkBaseMapper<T extends Link> {

    /**
     * Тип ссылок, который может обрабатывать этот маппер.
     *
     * @return значение {@link LinkType}
     */
    LinkType getSupportedType();

    /**
     * Заполняет общие поля объекта {@code T}.
     *
     * @param id первичный ключ
     * @param url оригинальный URL
     * @param lastModified время последнего изменения
     * @param version версия для оптимистической блокировки
     * @return экземпляр T с заполненными полями
     */
    T mapBase(Long id, String url, LocalDateTime lastModified, Long version);
}
