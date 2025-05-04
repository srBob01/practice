package backend.academy.scrapper.config;

import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.service.base.impl.jdbc.enrich.helper.LinkDetailsFetcher;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LinkDetailsFetcherConfig {
    @Bean
    public Map<LinkType, LinkDetailsFetcher> linkTypeLinkDetailsFetcherMap(
            List<LinkDetailsFetcher> linkDetailsFetchers) {
        return linkDetailsFetchers.stream()
                .collect(Collectors.toMap(LinkDetailsFetcher::getSupportedType, Function.identity()));
    }
}
