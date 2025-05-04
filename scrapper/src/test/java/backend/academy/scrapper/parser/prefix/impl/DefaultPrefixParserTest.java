package backend.academy.scrapper.parser.prefix.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.LinkType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DefaultPrefixParser — определение типа ссылки на основе реального префикса")
class DefaultPrefixParserTest {

    private final DefaultPrefixParser parser = new DefaultPrefixParser();

    @Test
    @DisplayName("Распознать префикс GitHub для полного URL-адреса “https://github.com/owner/repo”")
    void recognizeGithubPrefix() {
        // Arrange
        String url = "https://github.com/owner/repo";

        // Act
        LinkType type = parser.getSupportedType(url);

        // Assert
        assertThat(type).isEqualTo(LinkType.GITHUB);
    }

    @Test
    @DisplayName("Распознать префикс StackOverflow для полного URL-адреса “https://stackoverflow.com/questions/123”")
    void recognizeStackoverflowPrefix() {
        // Arrange
        String url = "https://stackoverflow.com/questions/123";

        // Act
        LinkType type = parser.getSupportedType(url);

        // Assert
        assertThat(type).isEqualTo(LinkType.STACKOVERFLOW);
    }

    @Test
    @DisplayName("Выдает исключение ScrapperException для неподдерживаемого URL-адреса “http://unknown/xyz”")
    void unsupportedPrefixThrows() {
        // Arrange
        String url = "http://unknown/xyz";

        // Act & Assert
        assertThatThrownBy(() -> parser.getSupportedType(url))
                .isInstanceOf(ScrapperException.class)
                .hasMessageContaining("Unsupported link format: " + url);
    }
}
