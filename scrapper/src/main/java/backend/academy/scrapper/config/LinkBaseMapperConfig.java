package backend.academy.scrapper.config;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.repository.jdbc.mapper.helper.LinkBaseMapper;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LinkBaseMapperConfig {
    @Bean
    public Map<LinkType, LinkBaseMapper<? extends Link>> linkBaseMapperMap(
            List<LinkBaseMapper<? extends Link>> linkBaseMappers) {
        return linkBaseMappers.stream()
                .collect(Collectors.toMap(LinkBaseMapper::getSupportedType, Function.identity()));
    }
}
