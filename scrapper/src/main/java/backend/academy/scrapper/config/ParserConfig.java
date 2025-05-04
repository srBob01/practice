package backend.academy.scrapper.config;

import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.parser.postfix.PostfixParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParserConfig {
    @Bean
    public Map<LinkType, List<PostfixParser>> mapLinkTypeToListParsers(List<PostfixParser> postfixParsers) {
        Map<LinkType, List<PostfixParser>> mapLinkTypeToListParsers = new HashMap<>();
        for (PostfixParser postfixParser : postfixParsers) {
            mapLinkTypeToListParsers
                    .computeIfAbsent(postfixParser.getSupportedType(), k -> new ArrayList<>())
                    .add(postfixParser);
        }
        return mapLinkTypeToListParsers;
    }
}
