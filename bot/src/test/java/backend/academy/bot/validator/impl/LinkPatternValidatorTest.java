package backend.academy.bot.validator.impl;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.bot.validator.ValidatorPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Валидатор паттерн-ссылок")
class LinkPatternValidatorTest {

    private LinkPatternValidator validator;

    @BeforeEach
    void setUp() {
        // Arrange common
        validator = new LinkPatternValidator();
    }

    @Nested
    @DisplayName("Когда ссылка соответствует GitHub или StackOverflow")
    class ValidLinks {

        @Test
        @DisplayName("возвращает true для GitHub-ссылки")
        void shouldReturnTrueForGithub() {
            // Arrange
            String githubUrl = "https://github.com/user/repo";

            // Act
            boolean result = validator.isValidLink(githubUrl);

            // Assert
            assertTrue(result, "Ожидалось true для GitHub-ссылки");
        }

        @Test
        @DisplayName("возвращает true для StackOverflow-ссылки")
        void shouldReturnTrueForStackOverflow() {
            // Arrange
            String soUrl = "https://stackoverflow.com/questions/12345";

            // Act
            boolean result = validator.isValidLink(soUrl);

            // Assert
            assertTrue(result, "Ожидалось true для StackOverflow-ссылки");
        }
    }

    @Nested
    @DisplayName("Когда ссылка не соответствует паттерну или равна null")
    class InvalidLinks {

        @Test
        @DisplayName("возвращает false для случайной ссылки")
        void shouldReturnFalseForRandomUrl() {
            // Arrange
            String randomUrl = "https://example.com";

            // Act
            boolean result = validator.isValidLink(randomUrl);

            // Assert
            assertFalse(result, "Ожидалось false для не-GitHub/не-StackOverflow ссылки");
        }

        @Test
        @DisplayName("возвращает false для пустой строки")
        void shouldReturnFalseForEmptyString() {
            // Arrange
            String empty = "";

            // Act
            boolean result = validator.isValidLink(empty);

            // Assert
            assertFalse(result, "Ожидалось false для пустой строки");
        }

        @Test
        @DisplayName("возвращает false для null")
        void shouldReturnFalseForNull() {
            // Arrange
            String nullUrl = null;

            // Act
            boolean result = validator.isValidLink(nullUrl);

            // Assert
            assertFalse(result, "Ожидалось false для null");
        }
    }

    @Test
    @DisplayName("приоритет FIRST")
    void shouldHaveFirstPriority() {
        // Act
        ValidatorPriority priority = validator.getPriority();

        // Assert
        assertEquals(ValidatorPriority.FIRST, priority, "Ожидался приоритет FIRST");
    }
}
