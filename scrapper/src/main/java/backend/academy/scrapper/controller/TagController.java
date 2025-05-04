package backend.academy.scrapper.controller;

import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.TagLinkRequest;
import backend.academy.dto.TagRequest;
import backend.academy.scrapper.service.base.ChatLinkService;
import backend.academy.scrapper.service.base.LinkTagService;
import backend.academy.scrapper.service.base.impl.manage.LinkManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {
    private final LinkManagementService linkManagementService;
    private final LinkTagService linkTagService;
    private final ChatLinkService chatLinkService;

    /**
     * Добавляет указанный тег к связи чат–ссылка.
     *
     * @param request DTO {@link TagLinkRequest} с chatId, URL ссылки и именем тега
     * @return строковое представление идентификатора созданной связи chatLink
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String addTagToLink(@Valid @RequestBody TagLinkRequest request) {
        return linkManagementService.addTagToLink(request).toString();
    }

    /**
     * Удаляет тег от конкретной связи чат–ссылка.
     *
     * @param chatId значение заголовка "Tg-Chat-Id" — идентификатор чата
     * @param link параметр запроса "link" — URL ссылки
     * @param tag параметр запроса "tag" — имя тега для удаления
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTagFromLink(
            @RequestHeader("Tg-Chat-Id") @NotNull Long chatId,
            @RequestParam("link") @NotBlank String link,
            @RequestParam("tag") @NotBlank String tag) {
        linkManagementService.deleteTagFromLink(chatId, link, tag);
    }

    /**
     * Возвращает список ссылок в чате, помеченных заданным тегом.
     *
     * @param tagRequest DTO {@link TagRequest} с chatId и именем тега
     * @return DTO {@link ListLinksResponse} с данными найденных ссылок
     */
    @GetMapping("/links")
    public ListLinksResponse getLinksByTag(@Valid @RequestBody TagRequest tagRequest) {
        return linkTagService.getLinksByTag(tagRequest);
    }

    /**
     * Удаляет все связи чат–ссылка в данном чате, у которых есть указанный тег.
     *
     * @param chatId значение заголовка "Tg-Chat-Id" — идентификатор чата
     * @param tag параметр запроса "tag" — имя тега
     */
    @DeleteMapping("/links")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLinksByTag(
            @RequestHeader("Tg-Chat-Id") @NotNull Long chatId, @RequestParam("tag") @NotBlank String tag) {
        chatLinkService.deleteChatLinksByTag(chatId, tag);
    }
}
