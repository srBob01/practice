package backend.academy.scrapper.service.base;

import backend.academy.scrapper.model.db.chatlink.ChatLink;
import java.util.List;
import java.util.Optional;

public interface ChatLinkService {
    /**
     * Создаёт связь между чатом и ссылкой.
     *
     * @param chatId идентификатор чата
     * @param linkId идентификатор ссылки
     * @return идентификатор новой сущности ChatLink
     */
    Long insertChatLink(Long chatId, Long linkId);

    /**
     * Удаляет подписку чата на ссылку.
     *
     * @param chatId идентификатор чата
     * @param linkId идентификатор ссылки
     */
    void deleteChatLink(Long chatId, Long linkId);

    /**
     * Возвращает список связей ChatLink для данного чата.
     *
     * @param chatId идентификатор чата
     * @return список сущностей ChatLink
     */
    List<ChatLink> getLinksByChatId(Long chatId);

    /**
     * Находит идентификатор связи ChatLink по chatId и оригинальному URL ссылки.
     *
     * @param chatId идентификатор чата
     * @param url оригинальный URL ссылки
     * @return Optional с идентификатором ChatLink, если связь найдена
     */
    Optional<Long> findIdChatLinkByChatIdAndUrl(Long chatId, String url);

    /**
     * Удаляет все подписки чата на ссылки, помеченные данным тегом.
     *
     * @param chatId идентификатор чата
     * @param tagName имя тега
     */
    void deleteChatLinksByTag(Long chatId, String tagName);

    /**
     * Возвращает список идентификаторов чатов, подписанных на данную ссылку.
     *
     * @param linkId идентификатор ссылки
     * @return список идентификаторов чатов
     */
    List<Long> getChatIdsByLinkId(Long linkId);
}
