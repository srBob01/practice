package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.helper.LinkChatTag;
import backend.academy.scrapper.repository.jdbc.mapper.LinkRowMapper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LinkTagJdbcRepository {
    private final JdbcClient jdbcClient;
    private final LinkRowMapper linkRowMapper;

    public void insertLinkTag(Long chatLinkId, Long tagId) {
        try {
            jdbcClient
                    .sql("INSERT INTO link_tag (chat_link_id, tag_id) VALUES (:chat_link_id, :tagId)")
                    .param("chat_link_id", chatLinkId)
                    .param("tagId", tagId)
                    .update();
        } catch (DuplicateKeyException e) {
            throw new ScrapperException("Связь уже существует", "Tag already added to this link");
        } catch (DataIntegrityViolationException e) {
            throw new ScrapperException("Некорректный link или tag", "Link or Tag does not exist");
        }
    }

    public Integer deleteLinkTag(Long chatLinkId, Long tagId) {
        return jdbcClient
                .sql("DELETE FROM link_tag WHERE chat_link_id = :chatLinkId AND tag_id = :tagId")
                .param("chatLinkId", chatLinkId)
                .param("tagId", tagId)
                .update();
    }

    public List<Link> findLinksByTag(Long chatId, String tagName) {
        return jdbcClient
                .sql(
                        """
                    SELECT l.*
                    FROM link l
                             JOIN chat_link cl ON cl.link_id = l.id
                             JOIN link_tag lt ON lt.chat_link_id = cl.id
                             JOIN tag t ON t.id = lt.tag_id
                    WHERE cl.chat_id = :chatId AND t.name = :tagName
                    ORDER BY l.id
                """)
                .param("chatId", chatId)
                .param("tagName", tagName)
                .query(linkRowMapper)
                .list();
    }

    /**
     * Строит отображение {@code chatLinkId → список имён тегов} для заданного набора связей.
     *
     * <ol>
     *   <li>Запрашивает все пары (chat_link_id, tag.name) из объединения таблиц {@code link_tag} и {@code tag}.
     *   <li>Группирует результат по {@code chatLinkIds} и собирает имена тегов в список.
     * </ol>
     *
     * @param chatLinkIds список идентификаторов записей {@code chat_link}
     * @return Map, где ключ – это {@code chatLinkIds}, значение – список имён тегов для этой связи
     */
    public Map<Long, List<String>> getTagsMapForChatLinks(List<Long> chatLinkIds) {
        if (chatLinkIds == null || chatLinkIds.isEmpty()) {
            return Map.of();
        }
        return jdbcClient
                .sql(
                        """
                    SELECT lt.chat_link_id, t.name
                    FROM link_tag lt
                    JOIN tag t ON t.id = lt.tag_id
                    WHERE lt.chat_link_id IN (:ids)
                """)
                .param("ids", chatLinkIds)
                .query((rs, rowNum) -> Map.entry(rs.getLong("chat_link_id"), rs.getString("name")))
                .list()
                .stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    /**
     * Строит вложенную мапу тегов по ссылкам и чатам: {@code linkId → (chatId → список имён тегов)}.
     *
     * <ol>
     *   <li>Делает LEFT JOIN таблицы {@code chat_link} с {@code link_tag} и {@code tag}, чтобы получить даже те
     *       чат–ссылка, у которых нет тегов.
     *   <li>Группирует результат сначала по {@code link_id}, затем по {@code chat_id}, собирая непустые имена тегов в
     *       списки.
     * </ol>
     *
     * @param linkIds список идентификаторов записей {@code link}
     * @return Map: {@code linkIds} → (Map: {@code chatId} → List<tagName>)
     */
    public Map<Long, Map<Long, List<String>>> getTagsGroupedByLinkAndChat(List<Long> linkIds) {
        if (linkIds == null || linkIds.isEmpty()) {
            return Map.of();
        }
        return jdbcClient
                .sql(
                        """
                    SELECT cl.link_id, cl.chat_id, t.name AS tag_name
                    FROM chat_link cl
                    LEFT JOIN link_tag lt ON lt.chat_link_id = cl.id
                    LEFT JOIN tag t ON t.id = lt.tag_id
                    WHERE cl.link_id IN (:linkIds)
                """)
                .param("linkIds", linkIds)
                .query((rs, rowNum) -> new LinkChatTag(
                        rs.getLong("link_id"), rs.getLong("chat_id"), rs.getString("tag_name") // может быть null
                        ))
                .list()
                .stream()
                .collect(Collectors.groupingBy(
                        LinkChatTag::linkId,
                        Collectors.groupingBy(
                                LinkChatTag::chatId,
                                Collectors.mapping(
                                        LinkChatTag::tag,
                                        Collectors.filtering(Objects::nonNull, Collectors.toList())))));
    }
}
