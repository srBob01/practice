package backend.academy.bot.config;

import backend.academy.bot.exception.model.ScrapperApiException;
import backend.academy.bot.service.client.ScrapperDeclarativeClient;
import backend.academy.dto.ApiErrorResponse;
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
public class ScrapperClientConfig {

    @Bean
    public ExchangeFilterFunction scrapperErrorFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is4xxClientError()) {
                return clientResponse.bodyToMono(ApiErrorResponse.class).flatMap(apiError -> {
                    log.error(
                            "Scrapper API error: code={}, exceptionName={}, exceptionMessage={}",
                            apiError.code(),
                            apiError.exceptionName(),
                            apiError.exceptionMessage());
                    return Mono.error(new ScrapperApiException(apiError));
                });
            }
            return Mono.just(clientResponse);
        });
    }

    @Bean
    public WebClient webClientForScrapper(
            WebClient.Builder webClientBuilder, BotConfig config, ExchangeFilterFunction scrapperErrorFilter) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.timeout().connect())
                .responseTimeout(Duration.ofMillis(config.timeout().read()));
        return webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(config.scrapperApiUrl())
                .filter(scrapperErrorFilter)
                .build();
    }

    @Bean
    public ScrapperDeclarativeClient scrapperDeclarativeClient(WebClient webClientForScrapper) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(
                        WebClientAdapter.create(webClientForScrapper))
                .build();
        return factory.createClient(ScrapperDeclarativeClient.class);
    }
}
