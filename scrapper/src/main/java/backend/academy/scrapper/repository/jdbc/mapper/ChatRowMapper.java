package backend.academy.scrapper.repository.jdbc.mapper;

import backend.academy.scrapper.model.db.chat.Chat;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/** RowMapper для сущности {@link Chat}. Просто считывает колонку {@code chat_id} и заполняет поле {@code id}. */
@Component
public class ChatRowMapper implements RowMapper<Chat> {
    @Override
    public Chat mapRow(ResultSet rs, int rowNum) throws SQLException {
        Chat chat = new Chat();
        chat.id(rs.getLong("chat_id"));
        return chat;
    }
}
