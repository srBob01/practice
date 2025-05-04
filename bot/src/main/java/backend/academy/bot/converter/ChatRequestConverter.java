package backend.academy.bot.converter;

import backend.academy.dto.ChatRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ChatRequestConverter implements Converter<Long, ChatRequest> {
    @Override
    public ChatRequest convert(@NotNull Long source) {
        return new ChatRequest(source);
    }
}
