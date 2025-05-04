package backend.academy.scrapper.config;

import backend.academy.scrapper.checkupdate.handler.LinkUpdateHandler;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.repository.jdbc.link.impl.LinkImplRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LinkTypeConfig {
    @Bean
    public Map<LinkType, LinkImplRepository<? extends Link>> linkImplRepositoryMap(
            List<LinkImplRepository<? extends Link>> linkImplRepositories) {
        return linkImplRepositories.stream()
                .collect(Collectors.toMap(LinkImplRepository::getSupportedType, Function.identity()));
    }

    @Bean
    public Map<LinkType, LinkUpdateHandler<? extends Link>> linkUpdateHandlerMap(
            List<LinkUpdateHandler<? extends Link>> linkImplRepositories) {
        return linkImplRepositories.stream()
                .collect(Collectors.toMap(LinkUpdateHandler::getSupportedType, Function.identity()));
    }
}
