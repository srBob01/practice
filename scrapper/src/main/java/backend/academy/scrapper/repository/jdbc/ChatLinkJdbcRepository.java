package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.repository.jdbc.mapper.ChatLinkRowMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatLinkJdbcRepository {
    private final JdbcClient jdbcClient;
    private final ChatLinkRowMapper chatLinkRowMapper;

    public Long insertChatLink(Long chatId, Long linkId) {
        try {
            return jdbcClient
                    .sql(
                            """
                        INSERT INTO chat_link (chat_id, link_id)
                        VALUES (:chat_id, :link_id)
                        RETURNING id
                    """)
                    .param("chat_id", chatId)
                    .param("link_id", linkId)
                    .query(Long.class)
                    .single();
        } catch (DataIntegrityViolationException e) {
            throw new ScrapperException(
                    "Ссылка для данного пользователя уже добавлена",
                    "User with id: " + chatId + " already subscribed to link " + linkId);
        }
    }

    public int deleteChatLink(Long chatId, Long linkId) {
        return jdbcClient
                .sql(
                        """
                    DELETE FROM chat_link
                    WHERE chat_id = :chat_id AND link_id = :link_id
                """)
                .param("chat_id", chatId)
                .param("link_id", linkId)
                .update();
    }

    public List<ChatLink> findByChatId(Long chatId) {
        return jdbcClient
                .sql(
                        """
                    SELECT cl.id as chat_link_id, l.*, c.id as chat_id
                    FROM chat_link cl
                    JOIN link l ON l.id = cl.link_id
                    JOIN chat c ON c.id = cl.chat_id
                    WHERE cl.chat_id = :chat_id
                    ORDER BY cl.id
                """)
                .param("chat_id", chatId)
                .query(chatLinkRowMapper)
                .list();
    }

    public Optional<Long> findChatLinkIdByChatIdAndUrl(Long chatId, String url) {
        return jdbcClient
                .sql(
                        """
                    SELECT cl.id
                    FROM chat_link cl
                    JOIN link l ON cl.link_id = l.id
                    WHERE cl.chat_id = :chatId AND l.original_url = :url
                """)
                .param("chatId", chatId)
                .param("url", url)
                .query(Long.class)
                .optional();
    }

    public int deleteChatLinksByTag(Long chatId, String tagName) {
        return jdbcClient
                .sql(
                        """
                    DELETE FROM chat_link
                    USING link_tag lt
                    JOIN tag t ON lt.tag_id = t.id
                    WHERE chat_link.id = lt.chat_link_id
                      AND chat_link.chat_id = :chatId
                      AND t.name = :tagName
                """)
                .param("chatId", chatId)
                .param("tagName", tagName)
                .update();
    }

    public List<Long> findChatIdsByLinkId(Long linkId) {
        return jdbcClient
                .sql(
                        """
                  SELECT chat_id
                    FROM chat_link
                   WHERE link_id = :linkId
                ORDER BY chat_id
                """)
                .param("linkId", linkId)
                .query(Long.class)
                .list();
    }
}
