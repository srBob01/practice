package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chat.Chat;
import backend.academy.scrapper.repository.jdbc.mapper.ChatRowMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatJdbcRepository {
    private final JdbcClient jdbcClient;
    private final ChatRowMapper chatRowMapper;

    public Chat addChat(Chat chat) {
        try {
            jdbcClient
                    .sql("INSERT INTO chat (id) VALUES (:id)")
                    .param("id", chat.id())
                    .update();
            return chat;
        } catch (DataIntegrityViolationException e) {
            throw new ScrapperException(
                    "Чат с таким ID уже существует",
                    "Попытка зарегистрировать уже существующий чат с id = " + chat.id());
        }
    }

    public int deleteChat(Long chatId) {
        return jdbcClient
                .sql("DELETE FROM chat WHERE id = :id")
                .param("id", chatId)
                .update();
    }

    public List<Chat> findAll() {
        return jdbcClient
                .sql("SELECT id as chat_id FROM chat")
                .query(chatRowMapper)
                .list();
    }
}
