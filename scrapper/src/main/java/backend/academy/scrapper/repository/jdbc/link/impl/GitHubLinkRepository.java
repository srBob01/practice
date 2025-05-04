package backend.academy.scrapper.repository.jdbc.link.impl;

import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GitHubLinkRepository implements LinkImplRepository<GitHubLink> {
    private final JdbcClient jdbcClient;

    @Override
    public void findByIds(List<Long> ids, Map<Long, GitHubLink> map) {
        jdbcClient
                .sql("SELECT * FROM github_link WHERE id IN (:ids)")
                .params(Map.of("ids", ids))
                .query((rs, rowNum) -> {
                    GitHubLink link = map.get(rs.getLong("id"));
                    link.owner(rs.getString("owner"));
                    link.repo(rs.getString("repo"));
                    link.itemNumber(rs.getString("item_number"));
                    link.eventType(GitHubEventType.valueOf(rs.getString("event_type")));
                    return link;
                })
                .list();
    }

    @Override
    public void insert(GitHubLink link) {
        try {
            jdbcClient
                    .sql("INSERT INTO github_link (id, owner, repo, item_number, event_type) "
                            + "VALUES (:link_id, :owner, :repo, :item_number, :event_type)")
                    .param("link_id", link.id())
                    .param("owner", link.owner())
                    .param("repo", link.repo())
                    .param("item_number", link.itemNumber())
                    .param("event_type", link.eventType().name())
                    .update();
        } catch (DataIntegrityViolationException e) {
            throw new ScrapperException("Некорректная вставка GitHub-ссылки", "Link ID is invalid or already used");
        }
    }

    @Override
    public LinkType getSupportedType() {
        return LinkType.GITHUB;
    }
}
