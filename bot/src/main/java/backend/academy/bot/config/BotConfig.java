package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(
        @NotEmpty String telegramToken,
        @NotEmpty String scrapperApiUrl,
        @NotNull Timeout timeout,
        @NotNull Kafka kafka,
        @NotNull Cache cache) {
    public record Timeout(@Positive int connect, @Positive int read) {}

    @Validated
    public record Kafka(
            @NotEmpty String name,
            @Positive int retryMaxAttempts,
            @Positive long retryBackoffMs,
            @Positive int partitions) {}

    public record Cache(@Positive int ttlMinutes) {}
}
