package backend.academy.scrapper.parser.postfix.abs;

import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.StackOverflowLink;
import backend.academy.scrapper.parser.postfix.PostfixParser;

/** Абстрактный базовый класс для специализированных парсеров StackOverflow (работающих с postfic). */
public abstract class AbstractStackOverflowSubPostfixParser implements PostfixParser {

    protected abstract boolean supportsPostfix(String postfix);

    protected abstract StackOverflowLink parsePostfix(String postfix);

    @Override
    public boolean supports(String url) {
        return supportsPostfix(url);
    }

    @Override
    public StackOverflowLink parse(String url) {
        return parsePostfix(url);
    }

    @Override
    public LinkType getSupportedType() {
        return LinkType.STACKOVERFLOW;
    }
}
