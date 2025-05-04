package backend.academy.scrapper.model.db.link.impl.type;

import lombok.Getter;

/** Перечисление типов событий GitHub с соответствующими шаблонами URL. */
@Getter
public enum GitHubEventType {
    ISSUE("https://api.github.com/repos/%s/%s/issues/%s"),
    PR("https://api.github.com/repos/%s/%s/pulls/%s"),
    REPO("https://api.github.com/repos/%s/%s");

    private final String endpointPattern;

    GitHubEventType(String endpointPattern) {
        this.endpointPattern = endpointPattern;
    }
}
