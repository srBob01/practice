package backend.academy.scrapper.repository.jpa.linktag;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.linktag.LinkTag;
import backend.academy.scrapper.model.helper.ChatLinkTagPair;
import backend.academy.scrapper.model.helper.LinkChatTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkTagJpaRepository extends JpaRepository<LinkTag, Long> {

    @Query("select lt.chatLink.link from LinkTag lt "
            + "where lt.chatLink.chat.id = :chatId and lt.tag.name = :tagName "
            + "order by lt.chatLink.link.id")
    List<Link> findLinksByTag(@Param("chatId") Long chatId, @Param("tagName") String tagName);

    @Query("select new backend.academy.scrapper.model.helper.ChatLinkTagPair(lt.chatLink.id, lt.tag.name) "
            + "from LinkTag lt where lt.chatLink.id in :chatLinkIds")
    List<ChatLinkTagPair> findTagsMapForChatLinks(@Param("chatLinkIds") List<Long> chatLinkIds);

    @Query(
            "select new backend.academy.scrapper.model.helper.LinkChatTag(lt.chatLink.link.id, lt.chatLink.chat.id, lt.tag.name) "
                    + "from LinkTag lt where lt.chatLink.link.id in :linkIds")
    List<LinkChatTag> findTagsGroupedByLinkAndChat(@Param("linkIds") List<Long> linkIds);

    @Modifying(clearAutomatically = true)
    @Query("delete from LinkTag lt where lt.chatLink.id = :chatLinkId and lt.tag.id = :tagId")
    int deleteLinkTagByChatLinkIdAndTagId(@Param("chatLinkId") Long chatLinkId, @Param("tagId") Long tagId);
}
