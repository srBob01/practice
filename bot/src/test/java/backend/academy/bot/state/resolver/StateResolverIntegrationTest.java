package backend.academy.bot.state.resolver;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.bot.config.StateConfig;
import backend.academy.bot.entity.State;
import backend.academy.bot.entity.StateMachine;
import backend.academy.bot.entity.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = StateResolverIntegrationTest.Config.class)
@DisplayName("StateResolver — интеграционный тест стратегий")
class StateResolverIntegrationTest {

    @Configuration
    @ComponentScan(basePackages = "backend.academy.bot.state.resolver.help.impl")
    @Import({StateResolver.class, StateConfig.class, StateMachine.class})
    static class Config {}

    @Autowired
    private StateResolver resolver;

    @Autowired
    private StateMachine stateMachine;

    private UserSession session;

    @Nested
    @DisplayName("NoSessionStateResolverHelper")
    class NoSessionTests {

        @Test
        @DisplayName("null-сессия + пустой текст → WITHOUT_START")
        void whenSessionNullAndBlank_thenWithoutStart() {
            // Arrange
            String text = "";
            UserSession sess = null;

            // Act
            State result = resolver.resolve(text, sess);

            // Assert
            assertEquals(State.WITHOUT_START, result);
        }

        @Test
        @DisplayName("null‑сессия + /start → START")
        void whenSessionNullAndStart_thenStart() {
            // Arrange
            String text = "/start";

            // Act
            State result = resolver.resolve(text, null);

            // Assert
            assertEquals(State.START, result);
        }

        @Test
        @DisplayName("null‑сессия + неизвестная команда → WITHOUT_START")
        void whenSessionNullAndUnknown_thenWithoutStart() {
            // Arrange
            String text = "/foo";

            // Act
            State result = resolver.resolve(text, null);

            // Assert
            assertEquals(State.WITHOUT_START, result);
        }
    }

    @Nested
    @DisplayName("CommandTextStateResolverHelper")
    class CommandTextTests {

        @BeforeEach
        void prepare() {
            session = new UserSession(State.LIST);
        }

        @Test
        @DisplayName("не в ожидании + известная команда → HELP")
        void whenNotWaitAndKnownCommand_thenHelp() {
            // Arrange
            String text = "/help";

            // Act
            State result = resolver.resolve(text, session);

            // Assert
            assertEquals(State.HELP, result);
        }

        @Test
        @DisplayName("не в ожидании + неизвестная команда → INCORRECT_INPUT")
        void whenNotWaitAndUnknownCommand_thenIncorrect() {
            // Arrange
            String text = "/foo";

            // Act
            State result = resolver.resolve(text, session);

            // Assert
            assertEquals(State.INCORRECT_INPUT, result);
        }

        @Test
        @DisplayName("в ожидании + /end → HELP")
        void whenWaitAndEnd_thenHelp() {
            // Arrange
            session.state(State.T_WAIT_LINK);
            String text = "/end";

            // Act
            State result = resolver.resolve(text, session);

            // Assert
            assertEquals(State.HELP, result);
        }

        @Test
        @DisplayName("в ожидании + другая команда → остаётся текущее")
        void whenWaitAndOtherCommand_thenRemain() {
            // Arrange
            session.state(State.T_WAIT_LINK);
            String text = "/help";

            // Act
            State result = resolver.resolve(text, session);

            // Assert
            assertEquals(State.T_WAIT_LINK, result);
        }
    }

    @Nested
    @DisplayName("NonCommandTextStateResolverHelper")
    class NonCommandTextTests {

        @BeforeEach
        void prepare() {
            session = new UserSession(State.A_WAIT_TAG);
        }

        @Test
        @DisplayName("в ожидании + произвольный текст → остаётся текущее")
        void whenWaitAndText_thenRemain() {
            // Arrange
            String text = "какой‑то ввод";
            // Act
            State result = resolver.resolve(text, session);
            // Assert
            assertEquals(State.A_WAIT_TAG, result);
        }

        @Test
        @DisplayName("не в ожидании + произвольный текст → INCORRECT_INPUT")
        void whenNotWaitAndText_thenIncorrect() {
            // Arrange
            session.state(State.HELP);
            // stateMachine.isWait(HELP) по умолчанию false
            String text = "hello";

            // Act
            State result = resolver.resolve(text, session);

            // Assert
            assertEquals(State.INCORRECT_INPUT, result);
        }
    }

    @Nested
    @DisplayName("FallbackStateResolverHelper")
    class FallbackTests {

        @BeforeEach
        void prepare() {
            session = new UserSession(State.HELP);
        }

        @Test
        @DisplayName("ни одна стратегия не поддерживает → INCORRECT_INPUT")
        void whenNothingSupports_thenIncorrect() {
            // Arrange
            String text = null; // NoSession не применится, CommandText и NonCommandText откажут

            // Act
            State result = resolver.resolve(text, session);

            // Assert
            assertEquals(State.INCORRECT_INPUT, result);
        }
    }
}
