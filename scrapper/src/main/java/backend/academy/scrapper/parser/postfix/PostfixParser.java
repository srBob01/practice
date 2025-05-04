package backend.academy.scrapper.parser.postfix;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;

/**
 * Парсер «postfix» части URL для создания конкретного объекта {@link Link}. Каждый парсер обрабатывает только URL‑части
 * своего типа.
 */
public interface PostfixParser {

    /**
     * Преобразует остаток URL (postfix) в объект {@link Link}.
     *
     * @param url часть URL после префикса
     * @return объект Link соответствующего подтипа
     */
    Link parse(String url);

    /**
     * Тип ссылок, которых касается данный парсер.
     *
     * @return {@link LinkType}
     */
    LinkType getSupportedType();

    /**
     * Проверяет, может ли данный парсер обработать переданный остаток URL.
     *
     * @param url часть URL после префикса
     * @return true, если парсер поддерживает этот формат postfix
     */
    boolean supports(String url);
}
