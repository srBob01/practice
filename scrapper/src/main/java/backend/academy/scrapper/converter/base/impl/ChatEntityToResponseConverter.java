package backend.academy.scrapper.converter.base.impl;

import backend.academy.dto.ChatResponse;
import backend.academy.scrapper.model.db.chat.Chat;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ChatEntityToResponseConverter implements Converter<Chat, ChatResponse> {
    @Override
    public ChatResponse convert(Chat source) {
        return new ChatResponse(source.id());
    }
}
