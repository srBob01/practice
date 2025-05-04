package backend.academy.scrapper.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        @NotEmpty String githubToken,
        @NotNull @Valid StackOverflowCredentials stackOverflow,
        @NotEmpty String botApiUrl,
        @NotNull @Valid Timeout timeout,
        @NotNull @Valid Update update,
        @NotNull @Valid Kafka kafka) {
    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}

    public record Timeout(@Positive int connect, @Positive int read) {}

    public record Update(
            @Positive int delayMs,
            @NotNull ProcessorType processorType,
            @NotNull DispatcherType dispatcherType,
            @Min(1) int threadCount,
            @Positive int intervalSeconds,
            @Positive int batchLimit) {
        public enum ProcessorType {
            SEQUENTIAL,
            PARALLEL
        }

        public enum DispatcherType {
            HTTP,
            KAFKA
        }
    }

    public record Kafka(
            @Positive int pollIntervalMs, @NotEmpty String name, @Positive int partitions, @Positive short replicas) {}
}
