package backend.academy.scrapper.repository.jdbc.mapper;

import backend.academy.scrapper.model.db.chat.Chat;
import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.model.db.link.Link;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * RowMapper для сущности {@link ChatLink}, связывающей {@link Chat} и {@link Link}. Использует {@link ChatRowMapper} и
 * {@link LinkRowMapper} для вложенных объектов.
 */
@Component
@RequiredArgsConstructor
public class ChatLinkRowMapper implements RowMapper<ChatLink> {

    private final LinkRowMapper linkRowMapper;
    private final ChatRowMapper chatRowMapper;

    @Override
    public ChatLink mapRow(@NotNull ResultSet rs, int rowNum) throws SQLException {
        Link link = linkRowMapper.mapRow(rs, rowNum);
        Chat chat = chatRowMapper.mapRow(rs, rowNum);

        ChatLink chatLink = new ChatLink();
        chatLink.id(rs.getLong("chat_link_id"));
        chatLink.chat(chat);
        chatLink.link(link);

        return chatLink;
    }
}
