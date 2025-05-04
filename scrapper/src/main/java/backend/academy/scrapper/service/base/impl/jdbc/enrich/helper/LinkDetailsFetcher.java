package backend.academy.scrapper.service.base.impl.jdbc.enrich.helper;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import java.util.List;

/** Интерфейс стратегии извлечения дополнительных данных для ссылок разных типов. */
public interface LinkDetailsFetcher {

    /**
     * Возвращает тип ссылок ({@link LinkType}), который может обрабатывать данный фетчер.
     *
     * @return тип ссылок, поддерживаемый этой стратегией
     */
    LinkType getSupportedType();

    /**
     * Обогащает список базовых объектов {@link Link} дополнительными полями, загруженными из специализированного
     * репозитория.
     *
     * @param baseLinks список исходных ссылок без деталей
     * @return тот же список, но с заполненными деталями внутри объектов Link
     */
    List<Link> fetchDetails(List<Link> baseLinks);
}
