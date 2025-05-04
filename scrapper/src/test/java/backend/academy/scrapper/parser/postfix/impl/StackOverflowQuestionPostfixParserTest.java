package backend.academy.scrapper.parser.postfix.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StackOverflowQuestionPostfixParser — разбор вопроса из полного URL")
class StackOverflowQuestionPostfixParserTest {

    private final StackOverflowQuestionPostfixParser parser = new StackOverflowQuestionPostfixParser();
    private static final String PREFIX = "https://stackoverflow.com/";

    @Test
    @DisplayName("supports() true для постфикса «questions/12345»")
    void supportsValidQuestion() {
        // Arrange
        String postfix = "questions/12345";
        // Act
        boolean supported = parser.supports(postfix);
        // Assert
        assertThat(supported).isTrue();
    }

    @Test
    @DisplayName("parse() создаёт StackOverflowLink из полного URL «https://stackoverflow.com/questions/987»")
    void parseCreatesSoLink() {
        // Arrange
        String fullUrl = PREFIX + "questions/987";
        String postfix = fullUrl.substring(PREFIX.length());
        // Act
        var link = parser.parse(postfix);
        // Assert
        assertThat(link.originalUrl()).isEqualTo(fullUrl);
        assertThat(link.questionId()).isEqualTo("987");
    }

    @Test
    @DisplayName("supports() false для некорректного постфикса «question/123»")
    void notSupportsOtherPaths() {
        // Arrange
        String wrong = "question/123";
        // Act
        boolean supported = parser.supports(wrong);
        // Assert
        assertThat(supported).isFalse();
    }
}
