package backend.academy.scrapper.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.config.ParserConfig;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.model.db.link.impl.StackOverflowLink;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = CompositeLinkParserTest.Config.class)
@DisplayName("CompositeLinkParser — component‑тест")
public class CompositeLinkParserTest {

    @TestConfiguration
    @ComponentScan(basePackages = "backend.academy.scrapper.parser")
    @Import(ParserConfig.class)
    static class Config {}

    @Autowired
    private CompositeLinkParser compositeLinkParser;

    @Test
    @DisplayName("полный GitHub‑Issue URL «https://github.com/owner/repo/issues/5»")
    void parseFullGithubIssue() {
        // Arrange
        String url = "https://github.com/owner/repo/issues/5";

        // Act
        Link link = compositeLinkParser.parse(url);

        // Assert
        assertThat(link).isInstanceOf(GitHubLink.class);
        GitHubLink gh = (GitHubLink) link;
        assertThat(gh.originalUrl()).isEqualTo(url);
        assertThat(gh.owner()).isEqualTo("owner");
        assertThat(gh.repo()).isEqualTo("repo");
        assertThat(gh.itemNumber()).isEqualTo("5");
        assertThat(gh.eventType()).isEqualTo(GitHubEventType.ISSUE);
    }

    @Test
    @DisplayName("полный GitHub‑PR URL «https://github.com/foo/bar/pull/7»")
    void parseFullGithubPr() {
        // Arrange
        String url = "https://github.com/foo/bar/pull/7";

        // Act
        Link link = compositeLinkParser.parse(url);

        // Assert
        assertThat(link).isInstanceOf(GitHubLink.class);
        GitHubLink gh = (GitHubLink) link;
        assertThat(gh.originalUrl()).isEqualTo(url);
        assertThat(gh.owner()).isEqualTo("foo");
        assertThat(gh.repo()).isEqualTo("bar");
        assertThat(gh.itemNumber()).isEqualTo("7");
        assertThat(gh.eventType()).isEqualTo(GitHubEventType.PR);
    }

    @Test
    @DisplayName("полный GitHub‑repo URL «https://github.com/foo/bar»")
    void parseGithubRepo() {
        // Arrange
        String url = "https://github.com/foo/bar";

        // Act
        Link link = compositeLinkParser.parse(url);

        // Assert
        assertThat(link).isInstanceOf(GitHubLink.class);
        GitHubLink gh = (GitHubLink) link;
        assertThat(gh.originalUrl()).isEqualTo(url);
        assertThat(gh.owner()).isEqualTo("foo");
        assertThat(gh.repo()).isEqualTo("bar");
        assertThat(gh.itemNumber()).isNull();
        assertThat(gh.eventType()).isEqualTo(GitHubEventType.REPO);
    }

    @Test
    @DisplayName("полный StackOverflow‑question URL «https://stackoverflow.com/questions/31415»")
    void parseStackOverflowQuestion() {
        // Arrange
        String url = "https://stackoverflow.com/questions/31415";

        // Act
        Link link = compositeLinkParser.parse(url);

        // Assert
        assertThat(link).isInstanceOf(StackOverflowLink.class);
        StackOverflowLink so = (StackOverflowLink) link;
        assertThat(so.originalUrl()).isEqualTo(url);
        assertThat(so.questionId()).isEqualTo("31415");
    }

    @Test
    @DisplayName("Unsupported format «http://unknown/xyz» бросает ScrapperException")
    void unsupportedFormatThrows() {
        // Arrange
        String url = "http://unknown/xyz";

        // Act & Assert
        assertThatThrownBy(() -> compositeLinkParser.parse(url))
                .isInstanceOf(ScrapperException.class)
                .hasMessageContaining("Unsupported link format: " + url);
    }
}
