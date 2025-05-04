package backend.academy.bot.service.client;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ChatRequest;
import backend.academy.dto.ChatResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.TagLinkRequest;
import backend.academy.dto.TagRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange
public interface ScrapperDeclarativeClient {

    @PostExchange("/tg-chat")
    Mono<ChatResponse> registerChat(@RequestBody ChatRequest chatRequest);

    @DeleteExchange("/tg-chat")
    Mono<Void> deleteChat(@RequestHeader("Tg-Chat-Id") long tgChatId);

    @GetExchange("/links")
    Mono<ListLinksResponse> getTrackedLinks(@RequestHeader("Tg-Chat-Id") long tgChatId);

    @PostExchange("/links")
    Mono<LinkResponse> addLink(@RequestHeader("Tg-Chat-Id") long tgChatId, @RequestBody AddLinkRequest addLinkRequest);

    @DeleteExchange("/links")
    Mono<Void> deleteLink(@RequestHeader("Tg-Chat-Id") long tgChatId, @RequestParam("link") String link);

    @PostExchange("/tag")
    Mono<String> addTag(@RequestBody TagLinkRequest request);

    @DeleteExchange("/tag")
    Mono<Void> deleteTag(
            @RequestHeader("Tg-Chat-Id") long tgChatId,
            @RequestParam("link") String link,
            @RequestParam("tag") String tag);

    @GetExchange(url = "/tag/links")
    Mono<ListLinksResponse> getLinksByTag(@RequestBody TagRequest request);

    @DeleteExchange(url = "/tag/links")
    Mono<Void> deleteLinksByTag(@RequestHeader("Tg-Chat-Id") long tgChatId, @RequestParam("tag") String tag);
}
