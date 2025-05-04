package backend.academy.scrapper.repository.jdbc.mapper;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.repository.jdbc.mapper.helper.LinkBaseMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * RowMapper для создания конкретного {@link Link} по данным из ResultSet. Определяет тип ссылки по колонке “type” и
 * делегирует создание экземпляра соответствующему {@link LinkBaseMapper}.
 */
@Component
@RequiredArgsConstructor
public class LinkRowMapper implements RowMapper<Link> {
    private final Map<LinkType, LinkBaseMapper<? extends Link>> linkBaseMapperMap;

    @Override
    public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        String originalUrl = rs.getString("original_url");
        LocalDateTime lastModified = rs.getTimestamp("last_modified").toLocalDateTime();
        Long version = rs.getLong("version");
        String typeStr = rs.getString("type");

        LinkType type = LinkType.valueOf(typeStr);
        return linkBaseMapperMap.get(type).mapBase(id, originalUrl, lastModified, version);
    }
}
