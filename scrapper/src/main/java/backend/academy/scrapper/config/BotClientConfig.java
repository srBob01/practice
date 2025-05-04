package backend.academy.scrapper.config;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.scrapper.exception.model.BotApiException;
import backend.academy.scrapper.service.client.BotDeclarativeClient;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Configuration
public class BotClientConfig {

    @Bean
    public ExchangeFilterFunction botErrorFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is4xxClientError()) {
                return clientResponse.bodyToMono(ApiErrorResponse.class).flatMap(apiError -> {
                    log.error(
                            "Bot API error: code={}, exceptionName={}, exceptionMessage={}",
                            apiError.code(),
                            apiError.exceptionName(),
                            apiError.exceptionMessage());
                    return Mono.error(new BotApiException(apiError));
                });
            }
            return Mono.just(clientResponse);
        });
    }

    @Bean
    public WebClient webClientForBot(
            WebClient.Builder webClientBuilder, ScrapperConfig config, ExchangeFilterFunction botErrorFilter) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.timeout().connect())
                .responseTimeout(Duration.ofMillis(config.timeout().read()));
        return webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(config.botApiUrl())
                .filter(botErrorFilter)
                .build();
    }

    @Bean
    public BotDeclarativeClient botDeclarativeClient(WebClient webClientForBot) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClientForBot))
                .build();
        return factory.createClient(BotDeclarativeClient.class);
    }
}
