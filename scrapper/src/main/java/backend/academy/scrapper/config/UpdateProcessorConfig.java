package backend.academy.scrapper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UpdateProcessorConfig {
    @Bean
    public int threadCount(ScrapperConfig scrapperConfig) {
        return scrapperConfig.update().threadCount();
    }

    @Bean
    public String topic(ScrapperConfig scrapperConfig) {
        return scrapperConfig.kafka().name();
    }

    @Bean
    public int intervalSeconds(ScrapperConfig scrapperConfig) {
        return scrapperConfig.update().intervalSeconds();
    }

    @Bean
    public int batchLimit(ScrapperConfig scrapperConfig) {
        return scrapperConfig.update().batchLimit();
    }
}
