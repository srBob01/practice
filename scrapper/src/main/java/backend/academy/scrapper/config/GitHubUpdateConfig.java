package backend.academy.scrapper.config;

import backend.academy.scrapper.checkupdate.worker.github.provider.GitHubUpdateProvider;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitHubUpdateConfig {
    @Bean
    public Map<GitHubEventType, GitHubUpdateProvider> githubUpdateProviderMap(List<GitHubUpdateProvider> providers) {
        return providers.stream()
                .collect(Collectors.toMap(GitHubUpdateProvider::getSupportedType, Function.identity()));
    }
}
