package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.outbox.OutboxMessage;
import backend.academy.scrapper.repository.jdbc.mapper.OutboxRowMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** JDBC-реализация доступа к таблице outbox с использованием JdbcClient. */
@Repository
@RequiredArgsConstructor
public class OutboxJdbcRepository {
    private final JdbcClient jdbcClient;
    private final OutboxRowMapper rowMapper;

    public OutboxMessage save(OutboxMessage msg) {
        try {
            jdbcClient
                    .sql(
                            """
                        INSERT INTO outbox (topic, payload, created_at)
                        VALUES (:topic, :payload, :createdAt)
                    """)
                    .param("topic", msg.topic())
                    .param("payload", msg.payload())
                    .param("createdAt", msg.createdAt())
                    .update();
            return msg;
        } catch (DataIntegrityViolationException e) {
            throw new ScrapperException("Не удалось записать в outbox", e.getMessage());
        }
    }

    public List<OutboxMessage> findUnprocessed(int limit) {
        return jdbcClient
                .sql(
                        """
                    SELECT id, topic, payload, created_at, processed_at
                    FROM outbox
                    WHERE processed_at IS NULL
                    ORDER BY created_at
                    LIMIT :limit
                """)
                .param("limit", limit)
                .query(rowMapper)
                .list();
    }

    public void markProcessed(Long id) {
        jdbcClient
                .sql("UPDATE outbox SET processed_at = CURRENT_TIMESTAMP WHERE id = :id")
                .param("id", id)
                .update();
    }
}
