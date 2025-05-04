package backend.academy.scrapper.repository.jdbc.link;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.repository.jdbc.mapper.LinkRowMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LinkJdbcRepository {
    private final JdbcClient jdbcClient;
    private final LinkRowMapper linkRowMapper;

    public Optional<Long> findIdByUrl(String originalUrl) {
        return jdbcClient
                .sql("SELECT id FROM link WHERE original_url = :url")
                .param("url", originalUrl)
                .query(Long.class)
                .optional();
    }

    public void insertLink(Link link) {
        try {
            Long id = jdbcClient
                    .sql(
                            """
                    INSERT INTO link (original_url, last_modified, last_checked, version, type)
                    VALUES (:original_url, :last_modified, CURRENT_TIMESTAMP, 0, :type)
                    RETURNING id
                    """)
                    .param("original_url", link.originalUrl())
                    .param("last_modified", link.lastModified())
                    .param("type", link.getType().name())
                    .query(Long.class)
                    .single();
            link.id(id);
        } catch (DataIntegrityViolationException e) {
            throw new ScrapperException(
                    "Ссылка с таким url уже существует",
                    "Попытка зарегистрировать уже существующую ссылку с url = " + link.originalUrl());
        }
    }

    /**
     * Выбирает до {@code limit} устаревших ссылок из таблицы {@code link}, блокируя их и пропуская заблокированные с
     * помощью SKIP LOCKED, и одновременно обновляет их поля:
     *
     * <ul>
     *   <li>{@code last_checked} устанавливается в текущий TIMESTAMP;
     *   <li>{@code version} инкрементируется на единицу.
     * </ul>
     *
     * Затем возвращает полный список полей этих ссылок как объекты {@link Link}.
     *
     * @param intervalSeconds минимальное число секунд с момента последней проверки, после которого ссылка считается
     *     устаревшей
     * @param limit максимальное число ссылок для обработки за один проход
     * @return список базовых сущностей {@link Link} для дальнейшей проверки обновлений
     */
    public List<Link> fetchLinksToUpdate(int intervalSeconds, int limit) {
        return jdbcClient
                .sql(
                        """
                    WITH selected AS (
                        SELECT *
                        FROM link
                        WHERE last_checked < (CURRENT_TIMESTAMP - make_interval(secs := :interval))
                        ORDER BY last_checked, id
                        FOR NO KEY UPDATE SKIP LOCKED
                        LIMIT :limit
                    )
                    UPDATE link
                    SET last_checked = CURRENT_TIMESTAMP,
                        version = link.version + 1
                    FROM selected s
                    WHERE link.id = s.id AND link.version = s.version
                    RETURNING link.*
                """)
                .param("interval", intervalSeconds)
                .param("limit", limit)
                .query(linkRowMapper)
                .list();
    }

    public void updateLastModified(Long linkId, LocalDateTime newTime) {
        jdbcClient
                .sql(
                        """
                    UPDATE link
                    SET last_modified = :new_time
                    WHERE id = :id
                """)
                .param("new_time", newTime)
                .param("id", linkId)
                .update();
    }
}
