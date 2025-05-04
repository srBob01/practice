package backend.academy.scrapper.parser.prefix;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.LinkType;

/** Компонент, отвечающий за распознавание типа ссылки по её префиксу. */
public interface PrefixParser {

    /**
     * Определяет {@link LinkType} для переданного URL на основании его начала (префикса).
     *
     * @param url полный URL
     * @return тип ссылки, который соответствует префиксу URL
     * @throws ScrapperException если ни один тип не подходит
     */
    LinkType getSupportedType(String url);
}
