package backend.academy.scrapper.repository.jdbc.link.impl;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.StackOverflowLink;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StackOverflowLinkRepository implements LinkImplRepository<StackOverflowLink> {
    private final JdbcClient jdbcClient;

    @Override
    public void findByIds(List<Long> ids, Map<Long, StackOverflowLink> map) {
        jdbcClient
                .sql("SELECT * FROM stackoverflow_link WHERE id IN (:ids)")
                .params(Map.of("ids", ids))
                .query((rs, rowNum) -> {
                    StackOverflowLink link = map.get(rs.getLong("id"));
                    link.questionId(rs.getString("question_id"));
                    return link;
                })
                .list();
    }

    @Override
    public void insert(StackOverflowLink stackOverflowLink) {
        try {
            jdbcClient
                    .sql("INSERT INTO stackoverflow_link (id, question_id) VALUES (:link_id, :question_id)")
                    .param("link_id", stackOverflowLink.id())
                    .param("question_id", stackOverflowLink.questionId())
                    .update();
        } catch (DataIntegrityViolationException e) {
            throw new ScrapperException("Некорректная вставка GitHub-ссылки", "Link ID is invalid or already used");
        }
    }

    @Override
    public LinkType getSupportedType() {
        return LinkType.STACKOVERFLOW;
    }
}
