package backend.academy.scrapper.parser.postfix.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GitHubRepoPostfixParser — разбор репозитория из полного URL")
class GitHubRepoPostfixParserTest {

    private final GitHubRepoPostfixParser parser = new GitHubRepoPostfixParser();
    private static final String PREFIX = "https://github.com/";

    @Test
    @DisplayName("supports() true для постфикса «alice/project»")
    void supportsValidRepo() {
        // Arrange
        String postfix = "alice/project";
        // Act
        boolean supported = parser.supports(postfix);
        // Assert
        assertThat(supported).isTrue();
    }

    @Test
    @DisplayName("parse() создаёт GitHubLink из полного URL «https://github.com/alice/project»")
    void parseCreatesRepoLink() {
        // Arrange
        String fullUrl = PREFIX + "alice/project";
        String postfix = fullUrl.substring(PREFIX.length());
        // Act
        GitHubLink link = parser.parse(postfix);
        // Assert
        assertThat(link.originalUrl()).isEqualTo(fullUrl);
        assertThat(link.eventType()).isEqualTo(GitHubEventType.REPO);
        assertThat(link.owner()).isEqualTo("alice");
        assertThat(link.repo()).isEqualTo("project");
        assertThat(link.itemNumber()).isNull();
    }

    @Test
    @DisplayName("supports() false для других шаблонов, например «owner/repo/issues/7»")
    void notSupportsOther() {
        // Arrange
        String wrong = "owner/repo/issues/7";
        // Act
        boolean supported = parser.supports(wrong);
        // Assert
        assertThat(supported).isFalse();
    }
}
