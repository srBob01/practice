package backend.academy.scrapper.service.base.impl.manage;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.TagLinkRequest;
import backend.academy.scrapper.converter.base.impl.LinkConverter;
import backend.academy.scrapper.converter.base.impl.ListLinksResponseConverter;
import backend.academy.scrapper.exception.model.ScrapperException;
import backend.academy.scrapper.model.db.chatlink.ChatLink;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.service.base.ChatLinkService;
import backend.academy.scrapper.service.base.LinkService;
import backend.academy.scrapper.service.base.LinkTagService;
import backend.academy.scrapper.service.base.TagService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LinkManagementService {
    private final LinkService linkService;
    private final LinkTagService linkTagService;
    private final ChatLinkService chatLinkService;
    private final TagService tagService;
    private final LinkConverter linkConverter;
    private final ListLinksResponseConverter listLinksResponseConverter;

    /**
     * Добавляет новую ссылку и связывает её с указанным чатом и тегами.
     *
     * <ol>
     *   <li>Создаёт или получает из {@link LinkService} запись {@link Link} по URL.
     *   <li>Создаёт связь чат–ссылка через {@link ChatLinkService#insertChatLink}.
     *   <li>Для каждого тега из запроса получает или создаёт его идентификатор через
     *       {@link TagService#getOrCreateTagId}.
     *   <li>Устанавливает связи «chatLink ↔ tag» через {@link LinkTagService#insertLinkTag}.
     * </ol>
     *
     * @param chatId идентификатор чата, для которого регистрируется ссылка
     * @param request DTO c URL ссылки и списком тегов
     * @return DTO {@link LinkResponse} с идентификатором ссылки и привязанными тегами
     */
    public LinkResponse addLink(Long chatId, AddLinkRequest request) {
        LinkResponse linkResponse = linkService.addLink(request);
        Long chatLinkId = chatLinkService.insertChatLink(chatId, linkResponse.id());
        for (String name : request.tags()) {
            Long tagId = tagService.getOrCreateTagId(name);
            linkTagService.insertLinkTag(chatLinkId, tagId);
        }
        return linkResponse;
    }

    /**
     * Возвращает все ссылки, привязанные к данному чату, вместе с их тегами.
     *
     * <ol>
     *   <li>Получает связи {@link ChatLink} для указанного chatId.
     *   <li>Извлекает набор chatLinkId и саму сущность {@link Link}.
     *   <li>Запрашивает у {@link LinkTagService#getTagsMapForLinks} теги для каждой связи.
     *   <li>Преобразует результат в список {@link LinkResponse} и оборачивает в {@link ListLinksResponse}.
     * </ol>
     *
     * @param chatId идентификатор чата
     * @return объект {@link ListLinksResponse} с данными всех ссылок и их тегов
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ListLinksResponse getLinksByChatId(Long chatId) {
        List<ChatLink> chatLinks = chatLinkService.getLinksByChatId(chatId);
        List<Long> chatLinkIds = chatLinks.stream().map(ChatLink::id).toList();
        Map<Long, Link> linkMap = chatLinks.stream().collect(Collectors.toMap(ChatLink::id, ChatLink::link));

        Map<Long, List<String>> tagsMap = linkTagService.getTagsMapForLinks(chatLinkIds);

        List<LinkResponse> responses = chatLinkIds.stream()
                .map(id -> linkConverter.convert(linkMap.get(id), tagsMap.getOrDefault(id, List.of())))
                .toList();

        return listLinksResponseConverter.convert(responses);
    }

    /**
     * Удаляет связь чат–ссылка по URL.
     *
     * <ol>
     *   <li>Ищет идентификатор {@link Link} по URL через {@link LinkService#findByUrl}.
     *   <li>Удаляет связь через {@link ChatLinkService#deleteChatLink}.
     * </ol>
     *
     * @param chatId идентификатор чата
     * @param url оригинальный URL ссылки
     * @throws ScrapperException если ссылка не найдена у данного чата
     */
    public void deleteLink(Long chatId, String url) {
        Long linkId = linkService
                .findByUrl(url)
                .orElseThrow(() -> new ScrapperException("Ссылка не найдена", "Link with URL not found"));

        chatLinkService.deleteChatLink(chatId, linkId);
    }

    /**
     * Добавляет существующий тег к связи чат–ссылка.
     *
     * <ol>
     *   <li>Ищет chatLinkId по chatId и URL через {@link ChatLinkService#findIdChatLinkByChatIdAndUrl}.
     *   <li>Получает или создаёт идентификатор тега через {@link TagService#getOrCreateTagId}.
     *   <li>Устанавливает связь через {@link LinkTagService#insertLinkTag}.
     * </ol>
     *
     * @param request DTO {@link TagLinkRequest} с chatId, URL и именем тега
     * @return идентификатор созданной связи chatLink
     * @throws ScrapperException если ссылка не найдена у пользователя
     */
    public Long addTagToLink(TagLinkRequest request) {
        Long chatLinkId = chatLinkService
                .findIdChatLinkByChatIdAndUrl(request.chatId(), request.link())
                .orElseThrow(() ->
                        new ScrapperException("Ссылка не найдена у пользователя", "No link found for chat and url"));

        Long tagId = tagService.getOrCreateTagId(request.tag());
        linkTagService.insertLinkTag(chatLinkId, tagId);
        return chatLinkId;
    }

    /**
     * Удаляет тег от связи чат–ссылка.
     *
     * <ol>
     *   <li>Ищет chatLinkId по chatId и URL через {@link ChatLinkService#findIdChatLinkByChatIdAndUrl}.
     *   <li>Ищет идентификатор тега по имени через {@link TagService#getTagIdByName}.
     *   <li>Удаляет связь через {@link LinkTagService#deleteLinkTag}.
     * </ol>
     *
     * @param chatId идентификатор чата
     * @param link URL ссылки
     * @param tag имя удаляемого тега
     * @throws ScrapperException если связь или тег не найдены
     */
    public void deleteTagFromLink(Long chatId, String link, String tag) {
        Long chatLinkId = chatLinkService
                .findIdChatLinkByChatIdAndUrl(chatId, link)
                .orElseThrow(() ->
                        new ScrapperException("Ссылка не найдена у пользователя", "No link found for chat and url"));

        Long tagId = tagService
                .getTagIdByName(tag)
                .orElseThrow(() -> new ScrapperException("Тэг не найден", "Tag not found: " + tag));

        linkTagService.deleteLinkTag(chatLinkId, tagId);
    }
}
