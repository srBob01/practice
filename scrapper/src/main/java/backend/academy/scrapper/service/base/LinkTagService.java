package backend.academy.scrapper.service.base;

import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.TagRequest;
import java.util.List;
import java.util.Map;

public interface LinkTagService {
    /**
     * Добавляет связь между записью chatLink и тегом.
     *
     * @param chatLinkId идентификатор связи чат–ссылка
     * @param tagId идентификатор тега
     */
    void insertLinkTag(Long chatLinkId, Long tagId);

    /**
     * Удаляет существующую связь между chatLink и тегом.
     *
     * @param chatLinkId идентификатор связи чат–ссылка
     * @param tagId идентификатор тега
     */
    void deleteLinkTag(Long chatLinkId, Long tagId);

    /**
     * Возвращает список ссылок, привязанных к заданному тегу в рамках одного чата.
     *
     * @param tagRequest DTO с идентификатором чата и именем тега
     * @return ответ с набором ссылок
     */
    ListLinksResponse getLinksByTag(TagRequest tagRequest);

    /**
     * Для каждого chatLinkId возвращает список имён тегов.
     *
     * @param chatLinkIds список идентификаторов chatLink
     * @return Map: chatLinkId → List<tagName>
     */
    Map<Long, List<String>> getTagsMapForLinks(List<Long> chatLinkIds);

    /**
     * Группирует теги по linkId и chatId.
     *
     * @param linkIds список идентификаторов ссылок
     * @return вложенную Map: linkId → (chatId → List<tagName>)
     */
    Map<Long, Map<Long, List<String>>> getTagsGroupedByLinkAndChat(List<Long> linkIds);
}
