package backend.academy.scrapper.parser;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.parser.postfix.PostfixParser;
import backend.academy.scrapper.parser.prefix.PrefixParser;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompositeLinkParser {

    private final PrefixParser prefixParser;
    private final Map<LinkType, List<PostfixParser>> parsersMap;

    /**
     * Универсальный парсер URL, который сначала определяет тип ссылки по префиксу, а затем делегирует обработку
     * соответствующему PostfixParser на основе остатка URL.
     *
     * <ol>
     *   <li>Вызывает {@link PrefixParser#getSupportedType(String)} для определения {@link LinkType}.
     *   <li>Извлекает «postfix» часть URL, обрезая префикс из {@code LinkType.prefix()}.
     *   <li>Ищет среди зарегистрированных {@link PostfixParser} того, чей {@link PostfixParser#supports(String)} вернёт
     *       true.
     *   <li>Вызывает у найденного парсера {@link PostfixParser#parse(String)} для создания объекта {@link Link}.
     * </ol>
     *
     * @param url полный URL, начинающийся с одного из известных префиксов
     * @return объект {@link Link} соответствующего типа
     * @throws ScrapperException если ни один PostfixParser не поддерживает остаток URL
     */
    public Link parse(String url) {
        LinkType linkType = prefixParser.getSupportedType(url);
        List<PostfixParser> postfixParsers = parsersMap.get(linkType);
        if (postfixParsers != null) {
            String postfix = url.substring(linkType.prefix().length());
            for (PostfixParser parser : postfixParsers) {
                if (parser.supports(postfix)) {
                    return parser.parse(postfix);
                }
            }
        }
        throw new ScrapperException("Некорректные параметры запроса", "Unsupported link format: " + url);
    }
}
