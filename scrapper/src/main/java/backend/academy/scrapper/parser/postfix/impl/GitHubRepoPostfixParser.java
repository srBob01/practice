package backend.academy.scrapper.parser.postfix.impl;

import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import backend.academy.scrapper.parser.postfix.abs.AbstractGitHubSubPostfixParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class GitHubRepoPostfixParser extends AbstractGitHubSubPostfixParser {

    // Ожидаем постфикс вида "owner/repo"
    private static final Pattern REPO_PATTERN = Pattern.compile("^([^/]+)/([^/]+)$");

    @Override
    protected boolean supportsPostfix(String postfix) {
        return REPO_PATTERN.matcher(postfix).matches();
    }

    @Override
    protected GitHubLink parsePostfix(String postfix) {
        Matcher matcher = REPO_PATTERN.matcher(postfix);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid GitHub repository postfix: " + postfix);
        }
        String owner = matcher.group(1);
        String repo = matcher.group(2);
        return new GitHubLink(LinkType.GITHUB.prefix() + postfix, owner, repo, null, GitHubEventType.REPO);
    }
}
