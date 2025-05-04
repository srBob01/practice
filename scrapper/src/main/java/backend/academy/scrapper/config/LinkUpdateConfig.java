package backend.academy.scrapper.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LinkUpdateConfig {

    @Bean
    @ConditionalOnProperty(name = "app.update.processor-type", havingValue = "PARALLEL")
    public ExecutorService linkUpdateExecutor(ScrapperConfig.Update update) {
        return Executors.newFixedThreadPool(update.threadCount());
    }

    @Bean
    public ScrapperConfig.Update updateConfig(ScrapperConfig cfg) {
        return cfg.update();
    }
}
