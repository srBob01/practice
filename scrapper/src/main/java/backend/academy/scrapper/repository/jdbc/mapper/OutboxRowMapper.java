package backend.academy.scrapper.repository.jdbc.mapper;

import backend.academy.scrapper.model.db.outbox.OutboxMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class OutboxRowMapper implements RowMapper<OutboxMessage> {
    @Override
    public OutboxMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
        OutboxMessage msg = new OutboxMessage();
        msg.id(rs.getLong("id"));
        msg.topic(rs.getString("topic"));
        msg.payload(rs.getString("payload"));
        msg.createdAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp proc = rs.getTimestamp("processed_at");
        if (proc != null) {
            msg.processedAt(proc.toLocalDateTime());
        }
        return msg;
    }
}
