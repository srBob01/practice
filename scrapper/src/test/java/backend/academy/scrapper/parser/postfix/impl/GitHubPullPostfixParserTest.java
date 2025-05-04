package backend.academy.scrapper.parser.postfix.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GitHubPullPostfixParser — разбор «pull» из полного URL")
class GitHubPullPostfixParserTest {

    private final GitHubPullPostfixParser parser = new GitHubPullPostfixParser();
    private static final String PREFIX = "https://github.com/";

    @Test
    @DisplayName("supports() true для постфикса «owner/repo/pull/7»")
    void supportsValidPrPostfix() {
        // Arrange
        String postfix = "owner/repo/pull/7";
        // Act
        boolean supported = parser.supports(postfix);
        // Assert
        assertThat(supported).isTrue();
    }

    @Test
    @DisplayName("parse() создаёт GitHubLink из полного URL «https://github.com/foo/bar/pull/7»")
    void parseCreatesPrLink() {
        // Arrange
        String fullUrl = PREFIX + "foo/bar/pull/7";
        String postfix = fullUrl.substring(PREFIX.length());
        // Act
        GitHubLink link = parser.parse(postfix);
        // Assert
        assertThat(link.originalUrl()).isEqualTo(fullUrl);
        assertThat(link.eventType()).isEqualTo(GitHubEventType.PR);
        assertThat(link.owner()).isEqualTo("foo");
        assertThat(link.repo()).isEqualTo("bar");
        assertThat(link.itemNumber()).isEqualTo("7");
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
