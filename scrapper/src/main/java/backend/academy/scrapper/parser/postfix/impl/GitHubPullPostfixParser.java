package backend.academy.scrapper.parser.postfix.impl;

import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import backend.academy.scrapper.parser.postfix.abs.AbstractGitHubSubPostfixParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class GitHubPullPostfixParser extends AbstractGitHubSubPostfixParser {

    // Ожидаем постфикс вида "owner/repo/pull/123"
    private static final Pattern PR_PATTERN = Pattern.compile("^([^/]+)/([^/]+)/pull/(\\d+)$");

    @Override
    protected boolean supportsPostfix(String postfix) {
        return PR_PATTERN.matcher(postfix).matches();
    }

    @Override
    protected GitHubLink parsePostfix(String postfix) {
        Matcher matcher = PR_PATTERN.matcher(postfix);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid GitHub PR postfix: " + postfix);
        }
        String owner = matcher.group(1);
        String repo = matcher.group(2);
        String itemNumber = matcher.group(3);
        return new GitHubLink(LinkType.GITHUB.prefix() + postfix, owner, repo, itemNumber, GitHubEventType.PR);
    }
}
