package backend.academy.bot.converter;

import backend.academy.dto.RemoveLinkRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RemoveLinkRequestConverter implements Converter<String, RemoveLinkRequest> {

    @Override
    public RemoveLinkRequest convert(@NotNull String url) {
        return new RemoveLinkRequest(url);
    }
}
