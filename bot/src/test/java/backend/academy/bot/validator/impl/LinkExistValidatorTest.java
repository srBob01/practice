package backend.academy.bot.validator.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@WireMockTest(httpPort = 0)
@DisplayName("Валидатор существования ссылки")
class LinkExistValidatorTest {

    private static final String EXISTING_PATH = "/existing";
    private static final String NOT_FOUND_PATH = "/not-found";

    private String baseUrl;
    private RestClient restClient;
    private LinkExistValidator validator;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        this.baseUrl = wmRuntimeInfo.getHttpBaseUrl();
        this.restClient = RestClient.builder().build();
        this.validator = new LinkExistValidator(restClient);
    }

    @Nested
    @DisplayName("Когда ссылка существует")
    class WhenLinkExists {

        @Test
        @DisplayName("возвращает true при ответе 2xx")
        void shouldReturnTrueOn2xx() {
            // Arrange
            stubFor(get(urlEqualTo(EXISTING_PATH)).willReturn(aResponse().withStatus(200)));

            String fullUrl = baseUrl + EXISTING_PATH;

            // Act
            boolean result = validator.isValidLink(fullUrl);

            // Assert
            assertTrue(result, "Ожидалось true для статуса 200");
        }
    }

    @Nested
    @DisplayName("Когда ссылка не найдена или происходит ошибка")
    class WhenLinkNotExistsOrError {

        @Test
        @DisplayName("возвращает false при ответе 4xx")
        void shouldReturnFalseOn4xx() {
            // Arrange
            stubFor(get(urlEqualTo(NOT_FOUND_PATH)).willReturn(aResponse().withStatus(404)));

            String fullUrl = baseUrl + NOT_FOUND_PATH;

            // Act
            boolean result = validator.isValidLink(fullUrl);

            // Assert
            assertFalse(result, "Ожидалось false для статуса 404");
        }

        @Test
        @DisplayName("возвращает false при сбое соединения")
        void shouldReturnFalseOnException() {
            // Arrange: эмулируем обрыв соединения
            stubFor(get(urlEqualTo(NOT_FOUND_PATH)).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

            String fullUrl = baseUrl + NOT_FOUND_PATH;

            // Act
            boolean result = validator.isValidLink(fullUrl);

            // Assert
            assertFalse(result, "Ожидалось false при сбое соединения");
        }
    }
}
