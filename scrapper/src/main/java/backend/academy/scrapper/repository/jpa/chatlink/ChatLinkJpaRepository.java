package backend.academy.scrapper.repository.jpa.chatlink;

import backend.academy.scrapper.model.db.chatlink.ChatLink;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLinkJpaRepository extends JpaRepository<ChatLink, Long> {
    List<ChatLink> findChatLinkByChatId(Long chatId);

    Optional<ChatLink> findChatLinkByChatIdAndLink_OriginalUrl(Long chatId, String originalUrl);

    @Modifying(clearAutomatically = true)
    @Query("delete from ChatLink cl where cl.chat.id = :chatId and cl.link.id = :linkId")
    int deleteChatLinkByChatIdAndLinkId(@Param("chatId") Long chatId, @Param("linkId") Long linkId);

    @Modifying(clearAutomatically = true)
    @Query("delete from ChatLink cl where cl.chat.id = :chatId and exists ("
            + "select lt from LinkTag lt where lt.chatLink = cl and lt.tag.name = :tagName)")
    int deleteChatLinksByTag(@Param("chatId") Long chatId, @Param("tagName") String tagName);

    @Query("SELECT cl.chat.id FROM ChatLink cl WHERE cl.link.id = :linkId ORDER BY cl.chat.id")
    List<Long> findChatIdsByLinkId(@Param("linkId") Long linkId);
}
