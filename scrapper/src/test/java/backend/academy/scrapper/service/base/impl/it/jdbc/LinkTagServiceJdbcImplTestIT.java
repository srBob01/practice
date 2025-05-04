package backend.academy.scrapper.service.base.impl.it.jdbc;

import static org.assertj.core.api.Assertions.*;

import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.TagRequest;
import backend.academy.scrapper.config.LinkBaseMapperConfig;
import backend.academy.scrapper.converter.base.impl.LinkConverter;
import backend.academy.scrapper.converter.base.impl.ListLinksResponseConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.repository.jdbc.LinkTagJdbcRepository;
import backend.academy.scrapper.repository.jdbc.mapper.LinkRowMapper;
import backend.academy.scrapper.repository.jdbc.mapper.helper.impl.GitHubLinkBaseMapper;
import backend.academy.scrapper.service.base.impl.jdbc.LinkTagServiceJdbcImpl;
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
@TestPropertySource(properties = {"spring.liquibase.enabled=false", "access-type=SQL"})
@Import({
    LinkTagServiceJdbcImpl.class,
    LinkTagJdbcRepository.class,
    LinkRowMapper.class,
    LinkConverter.class,
    ListLinksResponseConverter.class,
    LinkBaseMapperConfig.class,
    GitHubLinkBaseMapper.class
})
@ComponentScan("backend.academy.scrapper.repository.jdbc.mapper.helper")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("LinkTagServiceJdbcImpl — интеграционные тесты")
class LinkTagServiceJdbcImplTestIT {

    @SuppressWarnings("resource")
    @SuppressFBWarnings(
            value = "RV_RESOURCE_LEAK",
            justification = "PostgreSQLContainer lifecycle managed by Testcontainers")
    @ServiceConnection
    @Container
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private LinkTagServiceJdbcImpl service;

    @Test
    @DisplayName("insertLinkTag() — happy path")
    void insertHappy() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(10)").update();
        jdbc.sql(
                        """
                    INSERT INTO link(original_url,last_modified,last_checked,version,type)
                    VALUES('xx',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'GITHUB')
                """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='xx'")
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO chat_link(chat_id,link_id) VALUES(10,:lid)")
                .param("lid", linkId)
                .update();
        long chatLinkId = jdbc.sql(
                        """
                    SELECT id FROM chat_link
                     WHERE chat_id = 10 AND link_id = :lid
                """)
                .param("lid", linkId)
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO tag(name) VALUES('tagX')").update();
        long tagId = jdbc.sql("SELECT id FROM tag WHERE name='tagX'")
                .query(Long.class)
                .single();

        // Act
        service.insertLinkTag(chatLinkId, tagId);

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
        jdbc.sql("INSERT INTO chat(id) VALUES(11)").update();
        jdbc.sql(
                        """
                    INSERT INTO link(original_url,last_modified,last_checked,version,type)
                    VALUES('yy',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'GITHUB')
                """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='yy'")
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO chat_link(chat_id,link_id) VALUES(11,:lid)")
                .param("lid", linkId)
                .update();
        long chatLinkId = jdbc.sql(
                        """
                    SELECT id FROM chat_link
                     WHERE chat_id = 11 AND link_id = :lid
                """)
                .param("lid", linkId)
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO tag(name) VALUES('tagY')").update();
        long tagId = jdbc.sql("SELECT id FROM tag WHERE name='tagY'")
                .query(Long.class)
                .single();

        service.insertLinkTag(chatLinkId, tagId);

        // Act & Assert
        assertThatThrownBy(() -> service.insertLinkTag(chatLinkId, tagId)).isInstanceOf(ScrapperException.class);
    }

    @Test
    @DisplayName("deleteLinkTag() — happy & not found")
    void deleteTag() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(12)").update();
        jdbc.sql(
                        """
                    INSERT INTO link(original_url,last_modified,last_checked,version,type)
                    VALUES('zz',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'GITHUB')
                """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='zz'")
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO chat_link(chat_id,link_id) VALUES(12,:lid)")
                .param("lid", linkId)
                .update();
        long chatLinkId = jdbc.sql(
                        """
                    SELECT id FROM chat_link
                     WHERE chat_id = 12 AND link_id = :lid
                """)
                .param("lid", linkId)
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO tag(name) VALUES('tagZ')").update();
        long tagId = jdbc.sql("SELECT id FROM tag WHERE name='tagZ'")
                .query(Long.class)
                .single();

        service.insertLinkTag(chatLinkId, tagId);

        // Act — delete existing
        service.deleteLinkTag(chatLinkId, tagId);

        // Act & Assert — delete again should fail
        assertThatThrownBy(() -> service.deleteLinkTag(chatLinkId, tagId)).isInstanceOf(ScrapperException.class);
    }

    @Test
    @DisplayName("getLinksByTag()")
    void getLinksByTag() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(13)").update();
        jdbc.sql(
                        """
                    INSERT INTO link(original_url,last_modified,last_checked,version,type)
                    VALUES('u13',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'GITHUB')
                """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='u13'")
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO chat_link(chat_id,link_id) VALUES(13,:lid)")
                .param("lid", linkId)
                .update();
        long chatLinkId = jdbc.sql(
                        """
                    SELECT id FROM chat_link
                     WHERE chat_id = 13 AND link_id = :lid
                """)
                .param("lid", linkId)
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO tag(name) VALUES('tt')").update();
        long tagId =
                jdbc.sql("SELECT id FROM tag WHERE name='tt'").query(Long.class).single();

        service.insertLinkTag(chatLinkId, tagId);

        // Act
        ListLinksResponse resp = service.getLinksByTag(new TagRequest(13L, "tt"));

        // Assert
        assertThat(resp.links()).hasSize(1).extracting("url").containsExactly("u13");
    }

    @Test
    @DisplayName("getTagsMapForLinks() и getTagsGroupedByLinkAndChat()")
    void getMaps() {
        // Arrange
        jdbc.sql("INSERT INTO chat(id) VALUES(14),(15)").update();
        jdbc.sql(
                        """
                    INSERT INTO link(original_url,last_modified,last_checked,version,type)
                    VALUES('u14',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'GITHUB')
                """)
                .update();
        long linkId = jdbc.sql("SELECT id FROM link WHERE original_url='u14'")
                .query(Long.class)
                .single();

        jdbc.sql("INSERT INTO chat_link(chat_id,link_id) VALUES(14,:lid),(15,:lid)")
                .param("lid", linkId)
                .update();
        List<Long> chatLinkIds = jdbc.sql(
                        """
                    SELECT id FROM chat_link
                     WHERE link_id = :lid
                  ORDER BY chat_id
                """)
                .param("lid", linkId)
                .query(Long.class)
                .list();

        jdbc.sql("INSERT INTO tag(name) VALUES('m1'),('m2')").update();
        List<Long> tagIds =
                jdbc.sql("SELECT id FROM tag ORDER BY name").query(Long.class).list();

        service.insertLinkTag(chatLinkIds.get(0), tagIds.get(0));
        service.insertLinkTag(chatLinkIds.get(1), tagIds.get(1));

        // Act
        Map<Long, List<String>> tagMap = service.getTagsMapForLinks(chatLinkIds);
        Map<Long, Map<Long, List<String>>> nested = service.getTagsGroupedByLinkAndChat(List.of(linkId));

        // Assert
        assertThat(tagMap.get(chatLinkIds.get(0))).containsExactly("m1");
        assertThat(tagMap.get(chatLinkIds.get(1))).containsExactly("m2");

        assertThat(nested.get(linkId).get(14L)).containsExactly("m1");
        assertThat(nested.get(linkId).get(15L)).containsExactly("m2");
    }
}
