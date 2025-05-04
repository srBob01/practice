package backend.academy.scrapper.converter.base.impl;

import backend.academy.dto.ChatRequest;
import backend.academy.scrapper.model.db.chat.Chat;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ChatRequestToEntityConverter implements Converter<ChatRequest, Chat> {
    @Override
    public Chat convert(ChatRequest source) {
        return new Chat(source.id());
    }
}
