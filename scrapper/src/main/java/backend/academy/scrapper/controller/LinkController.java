package backend.academy.scrapper.controller;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.scrapper.service.base.impl.manage.LinkManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {
    private final LinkManagementService linkManagementService;

    /**
     * Возвращает список всех ссылок, зарегистрированных в данном чате, вместе с их тегами.
     *
     * @param chatId значение заголовка "Tg-Chat-Id" — идентификатор чата
     * @return DTO {@link ListLinksResponse} с данными по ссылкам и тегам
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") @NotNull Long chatId) {
        return linkManagementService.getLinksByChatId(chatId);
    }

    /**
     * Добавляет новую ссылку в указанный чат и сразу привязывает к ней теги.
     *
     * @param chatId значение заголовка "Tg-Chat-Id" — идентификатор чата
     * @param request DTO {@link AddLinkRequest} с URL ссылки и списком тегов
     * @return DTO {@link LinkResponse} с идентификатором ссылки, URL и тегами
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LinkResponse addLink(
            @RequestHeader("Tg-Chat-Id") @NotNull Long chatId, @Valid @RequestBody AddLinkRequest request) {
        return linkManagementService.addLink(chatId, request);
    }

    /**
     * Удаляет связь чат–ссылка по её URL.
     *
     * @param chatId значение заголовка "Tg-Chat-Id" — идентификатор чата
     * @param link параметр запроса "link" — оригинальный URL удаляемой ссылки
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLink(
            @RequestHeader("Tg-Chat-Id") @NotNull Long chatId, @RequestParam("link") @NotBlank String link) {
        linkManagementService.deleteLink(chatId, link);
    }
}
