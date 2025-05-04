package backend.academy.scrapper.repository.jdbc;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TagJdbcRepository {
    private final JdbcClient jdbcClient;

    /**
     * Вставляет новый тег с именем {@code name} или, если такой уже существует, возвращает его идентификатор. Операция
     * выполняется атомарно через CTE:
     *
     * <ol>
     *   <li>Пытается вставить новую запись в {@code tag}.
     *   <li>При конфликте по уникальному ключу ({@code name}) игнорирует вставку.
     *   <li>Возвращает идентификатор вставленного или уже существующего тега.
     * </ol>
     *
     * @param name имя тега, не пустое
     * @return идентификатор существующего или только что созданного тега
     */
    public Long insertOrGetIdByName(String name) {
        return jdbcClient
                .sql(
                        """
                WITH inserted AS (
                    INSERT INTO tag (name)
                    VALUES (:name)
                    ON CONFLICT (name) DO NOTHING
                    RETURNING id
                )
                SELECT id FROM inserted
                UNION
                SELECT id FROM tag WHERE name = :name
                """)
                .param("name", name)
                .query(Long.class)
                .single();
    }

    public Optional<Long> getIdByName(String name) {
        return jdbcClient
                .sql("SELECT id FROM tag WHERE name = :name")
                .param("name", name)
                .query(Long.class)
                .optional();
    }
}
