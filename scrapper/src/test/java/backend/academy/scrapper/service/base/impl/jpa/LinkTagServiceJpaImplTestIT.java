package backend.academy.scrapper.service.base.impl.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.TagRequest;
import backend.academy.scrapper.exception.model.ScrapperException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
        properties = {"spring.liquibase.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop", "access-type=ORM"})
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@DisplayName("LinkTagServiceJpaImpl — интеграционные тесты")
class LinkTagServiceJpaImplTestIT {

    @SuppressWarnings("resource")
    @SuppressFBWarnings(
            value = "RV_RESOURCE_LEAK",
            justification = "PostgreSQLContainer lifecycle managed by Testcontainers JUnit extension")
    @Container
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PG::getJdbcUrl);
        registry.add("spring.datasource.username", PG::getUsername);
        registry.add("spring.datasource.password", PG::getPassword);
    }

    static {
        PG.start();
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private LinkTagServiceJpaImpl service;

    @BeforeEach
    void cleanDb() {
        // Truncate everything so each @Test starts from a blank slate
        jdbc.execute(
                """
            TRUNCATE link_tag, tag, chat_link, link, chat
            RESTART IDENTITY CASCADE
            """);
    }

    @Test
    @DisplayName("insertLinkTag() — happy path")
    void insertHappy() {
        // given
        jdbc.update("INSERT INTO chat(id) VALUES (10)");
        jdbc.update(
                """
            INSERT INTO link(original_url,last_modified,last_checked,version,type)
            VALUES ('xx', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """);
        Long linkId = jdbc.queryForObject("SELECT id FROM link WHERE original_url='xx'", Long.class);
        jdbc.update("INSERT INTO chat_link(chat_id,link_id) VALUES (10,?)", linkId);
        Long clId = jdbc.queryForObject("SELECT id FROM chat_link WHERE chat_id=10 AND link_id=?", Long.class, linkId);
        jdbc.update("INSERT INTO tag(name) VALUES ('tagX')");
        Long tagId = jdbc.queryForObject("SELECT id FROM tag WHERE name='tagX'", Long.class);

        // when
        service.insertLinkTag(clId, tagId);

        // then
        Integer cnt = jdbc.queryForObject(
                "SELECT count(*) FROM link_tag WHERE chat_link_id=? AND tag_id=?", Integer.class, clId, tagId);
        assertThat(cnt).isOne();
    }

    @Test
    @DisplayName("insertLinkTag() — duplicate → ScrapperException")
    void insertDuplicate() {
        // given
        jdbc.update("INSERT INTO chat(id) VALUES (11)");
        jdbc.update(
                """
            INSERT INTO link(original_url,last_modified,last_checked,version,type)
            VALUES ('yy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """);
        Long linkId = jdbc.queryForObject("SELECT id FROM link WHERE original_url='yy'", Long.class);
        jdbc.update("INSERT INTO chat_link(chat_id,link_id) VALUES (11,?)", linkId);
        Long clId = jdbc.queryForObject("SELECT id FROM chat_link WHERE chat_id=11 AND link_id=?", Long.class, linkId);
        jdbc.update("INSERT INTO tag(name) VALUES ('tagY')");
        Long tagId = jdbc.queryForObject("SELECT id FROM tag WHERE name='tagY'", Long.class);

        // first insert succeeds
        service.insertLinkTag(clId, tagId);

        // when & then
        assertThatThrownBy(() -> service.insertLinkTag(clId, tagId)).isInstanceOf(ScrapperException.class);
    }

    @Test
    @DisplayName("deleteLinkTag() — happy & not found")
    void deleteTag() {
        // given
        jdbc.update("INSERT INTO chat(id) VALUES (12)");
        jdbc.update(
                """
            INSERT INTO link(original_url,last_modified,last_checked,version,type)
            VALUES ('zz', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """);
        Long linkId = jdbc.queryForObject("SELECT id FROM link WHERE original_url='zz'", Long.class);
        jdbc.update("INSERT INTO chat_link(chat_id,link_id) VALUES (12,?)", linkId);
        Long clId = jdbc.queryForObject("SELECT id FROM chat_link WHERE chat_id=12 AND link_id=?", Long.class, linkId);
        jdbc.update("INSERT INTO tag(name) VALUES ('tagZ')");
        Long tagId = jdbc.queryForObject("SELECT id FROM tag WHERE name='tagZ'", Long.class);

        service.insertLinkTag(clId, tagId);

        // first delete succeeds
        service.deleteLinkTag(clId, tagId);

        // second delete must throw
        assertThatThrownBy(() -> service.deleteLinkTag(clId, tagId)).isInstanceOf(ScrapperException.class);
    }

    @Test
    @DisplayName("getLinksByTag()")
    void getLinksByTag() {
        // given
        jdbc.update("INSERT INTO chat(id) VALUES (13)");
        jdbc.update(
                """
            INSERT INTO link(original_url,last_modified,last_checked,version,type)
            VALUES ('u13', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """);
        Long linkId = jdbc.queryForObject("SELECT id FROM link WHERE original_url='u13'", Long.class);
        jdbc.update("INSERT INTO chat_link(chat_id,link_id) VALUES (13,?)", linkId);
        Long clId = jdbc.queryForObject("SELECT id FROM chat_link WHERE chat_id=13 AND link_id=?", Long.class, linkId);
        jdbc.update("INSERT INTO tag(name) VALUES ('tt')");
        Long tagId = jdbc.queryForObject("SELECT id FROM tag WHERE name='tt'", Long.class);
        service.insertLinkTag(clId, tagId);

        // when
        ListLinksResponse resp = service.getLinksByTag(new TagRequest(13L, "tt"));

        // then
        assertThat(resp.links()).extracting("url").containsExactly("u13");
    }

    @Test
    @DisplayName("getTagsMapForLinks() и getTagsGroupedByLinkAndChat()")
    void getMaps() {
        // given
        jdbc.update("INSERT INTO chat(id) VALUES (14),(15)");
        jdbc.update(
                """
            INSERT INTO link(original_url,last_modified,last_checked,version,type)
            VALUES ('u14', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'GITHUB')
            """);
        Long linkId = jdbc.queryForObject("SELECT id FROM link WHERE original_url='u14'", Long.class);
        jdbc.update("INSERT INTO chat_link(chat_id,link_id) VALUES (14,?),(15,?)", linkId, linkId);

        List<Long> clIds =
                jdbc.queryForList("SELECT id FROM chat_link WHERE link_id=? ORDER BY chat_id", Long.class, linkId);

        jdbc.update("INSERT INTO tag(name) VALUES ('m1'),('m2')");
        // enforce known ordering
        List<String> tagNames = jdbc.queryForList("SELECT name FROM tag ORDER BY name", String.class);
        assertThat(tagNames).containsExactly("m1", "m2");

        Long ta = jdbc.queryForObject("SELECT id FROM tag WHERE name='m1'", Long.class);
        Long tb = jdbc.queryForObject("SELECT id FROM tag WHERE name='m2'", Long.class);

        service.insertLinkTag(clIds.get(0), ta);
        service.insertLinkTag(clIds.get(1), tb);

        // when
        Map<Long, List<String>> tagMap = service.getTagsMapForLinks(clIds);
        Map<Long, Map<Long, List<String>>> nested = service.getTagsGroupedByLinkAndChat(List.of(linkId));

        // then
        assertThat(tagMap.get(clIds.get(0))).containsExactly("m1");
        assertThat(tagMap.get(clIds.get(1))).containsExactly("m2");

        assertThat(nested.get(linkId).get(14L)).containsExactly("m1");
        assertThat(nested.get(linkId).get(15L)).containsExactly("m2");
    }
}
