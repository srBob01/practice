package backend.academy.scrapper.parser.prefix.impl;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.parser.prefix.PrefixParser;
import org.springframework.stereotype.Component;

@Component
public class DefaultPrefixParser implements PrefixParser {
    private static final String INVALID_PARAMETERS_QUERY = "Некорректные параметры запроса";
    private static final String UNSUPPORTED_LINK_FORMAT = "Unsupported link format: ";

    @Override
    public LinkType getSupportedType(String url) {
        for (LinkType type : LinkType.values()) {
            if (url.startsWith(type.prefix())) {
                return type;
            }
        }
        throw new ScrapperException(INVALID_PARAMETERS_QUERY, UNSUPPORTED_LINK_FORMAT + url);
    }
}
