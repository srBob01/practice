package backend.academy.scrapper.converter.base.impl;

import backend.academy.scrapper.model.db.chat.Chat;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ChatConverter implements Converter<Long, Chat> {
    @Override
    public Chat convert(@NotNull Long source) {
        return new Chat(source);
    }
}
