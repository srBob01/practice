package backend.academy.scrapper.parser.postfix.abs;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.parser.postfix.PostfixParser;

/** Абстрактный базовый класс для специализированных парсеров GitHub, работающих с постфиксом URL. */
public abstract class AbstractGitHubSubPostfixParser implements PostfixParser {

    /**
     * Проверяет поддержку конкретного постфикса.
     *
     * @param postfix часть URL после префикса (например, "owner/repo/issues/123")
     * @return true, если поддерживается
     */
    protected abstract boolean supportsPostfix(String postfix);

    /**
     * Разбирает постфикс и возвращает объект {@link Link}.
     *
     * @param postfix часть URL после префикса
     * @return объект Link
     */
    protected abstract GitHubLink parsePostfix(String postfix);

    @Override
    public boolean supports(String url) {
        return supportsPostfix(url);
    }

    @Override
    public GitHubLink parse(String url) {
        return parsePostfix(url);
    }

    @Override
    public LinkType getSupportedType() {
        return LinkType.GITHUB;
    }
}
