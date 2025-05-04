package backend.academy.bot.converter;

import backend.academy.bot.entity.UserSession;
import backend.academy.dto.AddLinkRequest;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AddLinkRequestConverter implements Converter<UserSession, AddLinkRequest> {

    @Override
    public AddLinkRequest convert(UserSession userSession) {
        String link = userSession.link();
        List<String> tags = userSession.tags();
        List<String> filters = userSession.filters();
        return new AddLinkRequest(link, tags, filters);
    }
}
