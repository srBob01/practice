package backend.academy.scrapper.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.config.BotClientConfig;
import backend.academy.scrapper.exception.model.BotApiException;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.test.StepVerifier;

@WireMockTest
@DisplayName("BotApiClient + BotErrorFilter через WireMock (отправка по одному)")
class BotApiClientTest {

    private final ExchangeFilterFunction botErrorFilter = new BotClientConfig().botErrorFilter();

    private BotApiClient client;

    @Test
    @DisplayName("AAA: success — отправляет уведомление и завершает Mono<Void>")
    void sendUpdateSuccess(WireMockRuntimeInfo wm) {
        // Arrange: WireMock вернет JSON-ответ "OK"
        stubFor(post("/updates")
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(okJson("\"OK\"")));

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + wm.getHttpPort())
                .filter(botErrorFilter)
                .build();
        var factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
                .build();
        client = new BotApiClient(factory.createClient(BotDeclarativeClient.class));

        var update = new LinkUpdate(1L, "u", "d", List.of());

        // Act & Assert
        StepVerifier.create(client.sendUpdate(update)).verifyComplete();

        // Verify JSON body
        verify(
                postRequestedFor(urlEqualTo("/updates"))
                        .withRequestBody(
                                equalToJson(
                                        """
                    {
                      "id": 1,
                      "url": "u",
                      "description": "d",
                      "chatIds": [ ]
                    }
                    """)));
    }

    @Test
    @DisplayName("error 4xx — бросает BotApiException при неуспешном ответе")
    void sendUpdateClientError(WireMockRuntimeInfo wm) {
        // Arrange: 400 и тело ApiErrorResponse
        var errorBody =
                """
            {"code":400,"exceptionName":"FooError","exceptionMessage":"bad request"}
            """;
        stubFor(post("/updates")
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorBody)));

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + wm.getHttpPort())
                .filter(botErrorFilter)
                .build();
        var factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
                .build();
        client = new BotApiClient(factory.createClient(BotDeclarativeClient.class));

        var update = new LinkUpdate(2L, "u2", "d2", List.of());

        // Act & Assert
        StepVerifier.create(client.sendUpdate(update))
                .expectErrorMatches(t -> t instanceof BotApiException
                        && ((BotApiException) t).errorResponse().exceptionName().equals("FooError"))
                .verify();
    }
}
