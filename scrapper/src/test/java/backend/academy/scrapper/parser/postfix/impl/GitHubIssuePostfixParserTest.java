package backend.academy.scrapper.parser.postfix.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GitHubIssuePostfixParser — разбор «issues» из полного URL")
class GitHubIssuePostfixParserTest {

    private final GitHubIssuePostfixParser parser = new GitHubIssuePostfixParser();
    private static final String PREFIX = "https://github.com/";

    @Test
    @DisplayName("supports() true для постфикса «owner/repo/issues/42»")
    void supportsValidIssuePostfix() {
        // Arrange
        String postfix = "owner/repo/issues/42";
        // Act
        boolean supported = parser.supports(postfix);
        // Assert
        assertThat(supported).isTrue();
    }

    @Test
    @DisplayName("supports() false для некорректного постфикса «owner/repo/issue/42»")
    void notSupportsInvalidIssuePostfix() {
        // Arrange
        String invalid = "owner/repo/issue/42";
        // Act
        boolean supported = parser.supports(invalid);
        // Assert
        assertThat(supported).isFalse();
    }

    @Test
    @DisplayName("parse() создаёт GitHubLink из полного URL «https://github.com/ownerX/repoY/issues/123»")
    void parseCreatesGitHubLink() {
        // Arrange
        String fullUrl = PREFIX + "ownerX/repoY/issues/123";
        String postfix = fullUrl.substring(PREFIX.length());
        // Act
        GitHubLink link = parser.parse(postfix);
        // Assert
        assertThat(link.originalUrl()).isEqualTo(fullUrl);
        assertThat(link.owner()).isEqualTo("ownerX");
        assertThat(link.repo()).isEqualTo("repoY");
        assertThat(link.itemNumber()).isEqualTo("123");
        assertThat(link.eventType()).isEqualTo(GitHubEventType.ISSUE);
    }
}
