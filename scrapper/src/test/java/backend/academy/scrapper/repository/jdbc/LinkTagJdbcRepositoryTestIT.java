package backend.academy.scrapper.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.config.LinkBaseMapperConfig;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.repository.jdbc.mapper.LinkRowMapper;
import backend.academy.scrapper.repository.jdbc.mapper.helper.impl.GitHubLinkBaseMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Sql({
    "classpath:db/changelog/changeset/001-create-chat-table.sql",
    "classpath:db/changelog/changeset/002-create-link-table.sql",
    "classpath:db/changelog/changeset/003-create-chat_link-table.sql",
    "classpath:db/changelog/changeset/004-create-tag-table.sql",
    "classpath:db/changelog/changeset/005-create-link_tag-table.sql"
})
@TestPropertySource(properties = "spring.liquibase.enabled=false")
@Import({LinkTagJdbcRepository.class, LinkRowMapper.class, LinkBaseMapperConfig.class, GitHubLinkBaseMapper.class})
@ComponentScan("backend.academy.scrapper.repository.jdbc.mapper.helper")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("LinkTagJdbcRepository — интеграционные тесты")
class LinkTagJdbcRepositoryTestIT {

    @SuppressWarnings("resource")
    @SuppressFBWarnings(value = "RV_RESOURCE_LEAK", justification = "Testcontainers lifecycle")
    @ServiceConnection
    @Container
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private LinkTagJdbcRepository repo;

    @Test
    @DisplayName("insertLinkTag() — happy path")
    void insertHappy() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(1)").update();
        jdbc.sql(
                        """
                INSERT INTO link(original_url,last_modified,last_checked,version,type)
                VALUES('u',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'GITHUB')
            """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='u'")
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO chat_link(chat_id,link_id) VALUES(1,:lid)")
                .param("lid", linkId)
                .update();
        long chatLinkId = jdbc.sql(
                        """
                SELECT id
                  FROM chat_link
                 WHERE chat_id = 1 AND link_id = :lid
            """)
                .param("lid", linkId)
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO tag(name) VALUES('t1')").update();
        long tagId =
                jdbc.sql("SELECT id FROM tag WHERE name='t1'").query(Long.class).single();

        // Act
        repo.insertLinkTag(chatLinkId, tagId);

        // Assert
        int cnt = jdbc.sql(
                        """
                    SELECT count(*)
                      FROM link_tag
                     WHERE chat_link_id = :clid
                       AND tag_id = :tid
                """)
                .param("clid", chatLinkId)
                .param("tid", tagId)
                .query(Integer.class)
                .single();
        assertThat(cnt).isOne();
    }

    @Test
    @DisplayName("insertLinkTag() — duplicate → ScrapperException")
    void insertDuplicate() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(2)").update();
        jdbc.sql(
                        """
                INSERT INTO link(original_url,last_modified,last_checked,version,type)
                VALUES('dup',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'GITHUB')
            """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='dup'")
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO chat_link(chat_id,link_id) VALUES(2,:lid)")
                .param("lid", linkId)
                .update();
        long chatLinkId = jdbc.sql(
                        """
                SELECT id
                  FROM chat_link
                 WHERE chat_id = 2 AND link_id = :lid
            """)
                .param("lid", linkId)
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO tag(name) VALUES('t2')").update();
        long tagId =
                jdbc.sql("SELECT id FROM tag WHERE name='t2'").query(Long.class).single();

        // pre-insert
        repo.insertLinkTag(chatLinkId, tagId);

        // Act & Assert
        assertThatThrownBy(() -> repo.insertLinkTag(chatLinkId, tagId)).isInstanceOf(ScrapperException.class);
    }

    @Test
    @DisplayName("deleteLinkTag() — happy & no-op")
    void deleteTag() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(3)").update();
        jdbc.sql(
                        """
                INSERT INTO link(original_url,last_modified,last_checked,version,type)
                VALUES('d',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'GITHUB')
            """)
                .update();
        long lid = jdbc.sql("SELECT id FROM link WHERE original_url='d'")
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO chat_link(chat_id,link_id) VALUES(3,:lid)")
                .param("lid", lid)
                .update();
        long chatLinkId = jdbc.sql(
                        """
                SELECT id
                  FROM chat_link
                 WHERE chat_id = 3 AND link_id = :lid
            """)
                .param("lid", lid)
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO tag(name) VALUES('t3')").update();
        long tid =
                jdbc.sql("SELECT id FROM tag WHERE name='t3'").query(Long.class).single();

        repo.insertLinkTag(chatLinkId, tid);

        // Act & Assert
        assertThat(repo.deleteLinkTag(chatLinkId, tid)).isOne();
        assertThat(repo.deleteLinkTag(chatLinkId, tid)).isZero();
    }

    @Test
    @DisplayName("findLinksByTag()")
    void findLinksByTag() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(4)").update();
        jdbc.sql(
                        """
                INSERT INTO link(original_url,last_modified,last_checked,version,type)
                VALUES('url4',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'GITHUB')
            """)
                .update();
        long lid = jdbc.sql("SELECT id FROM link WHERE original_url='url4'")
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO chat_link(chat_id,link_id) VALUES(4,:lid)")
                .param("lid", lid)
                .update();
        long chatLinkId = jdbc.sql(
                        """
                SELECT id
                  FROM chat_link
                 WHERE chat_id = 4 AND link_id = :lid
            """)
                .param("lid", lid)
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO tag(name) VALUES('t4')").update();
        long tid =
                jdbc.sql("SELECT id FROM tag WHERE name='t4'").query(Long.class).single();

        repo.insertLinkTag(chatLinkId, tid);

        // Act
        List<Link> result = repo.findLinksByTag(4L, "t4");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(lid);
    }

    @Test
    @DisplayName("getTagsMapForChatLinks()")
    void getTagsMap() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(5)").update();
        jdbc.sql(
                        """
                INSERT INTO link(original_url,last_modified,last_checked,version,type)
                VALUES('u5',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'GITHUB')
            """)
                .update();
        long lid = jdbc.sql("SELECT id FROM link WHERE original_url='u5'")
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO chat_link(chat_id,link_id) VALUES(5,:lid)")
                .param("lid", lid)
                .update();
        long clid = jdbc.sql("SELECT id FROM chat_link WHERE link_id = :lid")
                .param("lid", lid)
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO tag(name) VALUES('x'),('y')").update();
        List<Long> tids =
                jdbc.sql("SELECT id FROM tag ORDER BY name").query(Long.class).list();
        for (Long tid : tids) {
            repo.insertLinkTag(clid, tid);
        }

        // Act
        Map<Long, List<String>> map = repo.getTagsMapForChatLinks(List.of(clid));

        // Assert
        assertThat(map).containsKey(clid);
        assertThat(map.get(clid)).containsExactly("x", "y");
    }

    @Test
    @DisplayName("getTagsGroupedByLinkAndChat()")
    void getGroupedMap() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(6),(7)").update();
        jdbc.sql(
                        """
                INSERT INTO link(original_url,last_modified,last_checked,version,type)
                VALUES('u6',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'GITHUB')
            """)
                .update();
        long lid = jdbc.sql("SELECT id FROM link WHERE original_url='u6'")
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO chat_link(chat_id,link_id) VALUES(6,:lid),(7,:lid)")
                .param("lid", lid)
                .update();
        List<Long> clids = jdbc.sql(
                        """
                SELECT id
                  FROM chat_link
                 WHERE link_id = :lid
              ORDER BY chat_id
            """)
                .param("lid", lid)
                .query(Long.class)
                .list();

        jdbc.sql("INSERT INTO tag(name) VALUES('a'),('b')").update();
        List<Long> allTids =
                jdbc.sql("SELECT id FROM tag ORDER BY name").query(Long.class).list();

        // give 'a' to first chat_link, 'b' to second
        repo.insertLinkTag(clids.get(0), allTids.get(0));
        repo.insertLinkTag(clids.get(1), allTids.get(1));

        // Act
        Map<Long, Map<Long, List<String>>> nested = repo.getTagsGroupedByLinkAndChat(List.of(lid));

        // Assert
        assertThat(nested).containsKey(lid);
        Map<Long, List<String>> inner = nested.get(lid);
        assertThat(inner.get(6L)).containsExactly("a");
        assertThat(inner.get(7L)).containsExactly("b");
    }
}
