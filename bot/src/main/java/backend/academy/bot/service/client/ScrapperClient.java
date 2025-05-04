package backend.academy.bot.service.client;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ChatRequest;
import backend.academy.dto.ChatResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.TagLinkRequest;
import backend.academy.dto.TagRequest;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

/**
 * Клиент для взаимодействия с API Scrapper с поддержкой кэширования и инвалидации кеша.
 *
 * <p>Методы получения списков ссылок кэшируются в Spring Cache с именами "trackedLinks" и "linksByTag". При изменении
 * данных (добавление/удаление чата, ссылок или тегов) соответствующие записи в кэше инвалидируются через
 * {@link CacheManager} и Redis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapperClient {

    private final ScrapperDeclarativeClient client;
    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Регистрирует новый чат в сервисе Scrapper и инвалидирует кешы связанных запросов.
     *
     * @param chatRequest объект запроса с данными чата
     * @return результат регистрации чата
     */
    public Mono<ChatResponse> registerChat(ChatRequest chatRequest) {
        return client.registerChat(chatRequest)
                .doOnSuccess(chatResponse -> {
                    log.info("Successfully registered chat: {}", chatResponse.id());
                    Objects.requireNonNull(cacheManager.getCache("trackedLinks"))
                            .evict(chatRequest.id());
                    String pattern = "linksByTag::" + chatRequest.id() + "_*";
                    var keys = redisTemplate.keys(pattern);
                    if (!CollectionUtils.isEmpty(keys)) {
                        redisTemplate.delete(keys);
                    }
                })
                .doOnError(e -> log.error("Error registering chat for {}", chatRequest.id()));
    }

    /**
     * Удаляет чат из сервиса Scrapper и инвалидирует кеши, связанные с этим чатом.
     *
     * @param tgChatId идентификатор Telegram-чата
     * @return Mono<Void> после выполнения операции
     */
    public Mono<Void> deleteChat(long tgChatId) {
        return client.deleteChat(tgChatId)
                .doOnSuccess(__ -> {
                    log.info("Successfully deleted chat: {}", tgChatId);
                    Objects.requireNonNull(cacheManager.getCache("trackedLinks"))
                            .evict(tgChatId);
                    String pattern = "linksByTag::" + tgChatId + "_*";
                    var keys = redisTemplate.keys(pattern);
                    if (!CollectionUtils.isEmpty(keys)) {
                        redisTemplate.delete(keys);
                    }
                })
                .doOnError(e -> log.error("Error deleting chat for {}", tgChatId));
    }

    /**
     * Получает список отслеживаемых ссылок для указанного чата и кэширует его.
     *
     * @param tgChatId идентификатор Telegram-чата
     * @return Mono со списком ссылок
     */
    @Cacheable(cacheNames = "trackedLinks", key = "#tgChatId")
    public Mono<ListLinksResponse> getTrackedLinks(long tgChatId) {
        return client.getTrackedLinks(tgChatId)
                .doOnSuccess(s -> log.info("Successfully fetched tracked links for chat: {}", tgChatId))
                .doOnError(e -> log.error("Error fetching tracked links for chat {}", tgChatId));
    }

    /**
     * Добавляет новую ссылку в указанном чате, инвалидирует кеши "trackedLinks" и "linksByTag".
     *
     * @param tgChatId идентификатор Telegram-чата
     * @param addLinkRequest объект запроса для добавления ссылки, содержит теги
     * @return Mono с информацией о добавленной ссылке
     */
    public Mono<LinkResponse> addLink(long tgChatId, AddLinkRequest addLinkRequest) {
        return client.addLink(tgChatId, addLinkRequest)
                .doOnSuccess(s -> {
                    log.info("Successfully added link for chat: {}", tgChatId);
                    Objects.requireNonNull(cacheManager.getCache("trackedLinks"))
                            .evict(tgChatId);
                    addLinkRequest.tags().forEach(tag -> {
                        String key = tgChatId + "_" + tag;
                        Objects.requireNonNull(cacheManager.getCache("linksByTag"))
                                .evict(key);
                    });
                })
                .doOnError(e -> log.error("Error adding link for chat {}", tgChatId));
    }

    /**
     * Удаляет ссылку из указанного чата и инвалидирует соответствующие кеши.
     *
     * @param tgChatId идентификатор Telegram-чата
     * @param link URL ссылки для удаления
     * @return Mono<Void> после выполнения операции
     */
    public Mono<Void> deleteLink(long tgChatId, String link) {
        return client.deleteLink(tgChatId, link)
                .doOnSuccess(__ -> {
                    log.info("Successfully removed link for chat: {}", tgChatId);
                    Objects.requireNonNull(cacheManager.getCache("trackedLinks"))
                            .evict(tgChatId);
                    String pattern = "linksByTag::" + tgChatId + "_*";
                    var keys = redisTemplate.keys(pattern);
                    if (!CollectionUtils.isEmpty(keys)) {
                        redisTemplate.delete(keys);
                    }
                })
                .doOnError(e -> log.error("Error removing link for chat {}", tgChatId));
    }

    /**
     * Добавляет тег к ссылке и инвалидирует кеш "linksByTag" для данного чата и тега.
     *
     * @param request объект запроса с chatId, ссылкой и тегом
     * @return Mono с результатом операции (строка-тэг)
     */
    public Mono<String> addTag(TagLinkRequest request) {
        return client.addTag(request)
                .doOnSuccess(s -> {
                    log.info("Added tag for link {}", request.link());
                    String key = request.chatId() + "_" + request.tag();
                    Objects.requireNonNull(cacheManager.getCache("linksByTag")).evict(key);
                })
                .doOnError(e -> log.error("Error adding tag for link {}", request.link()));
    }

    /**
     * Удаляет тег у ссылки и инвалидирует кеш "linksByTag" для данного чата и тега.
     *
     * @param chatId идентификатор Telegram-чата
     * @param link URL ссылки
     * @param tag удаляемый тег
     * @return Mono<Void> после выполнения операции
     */
    public Mono<Void> deleteTag(long chatId, String link, String tag) {
        return client.deleteTag(chatId, link, tag)
                .doOnSuccess(__ -> {
                    log.info("Deleted tag for link {}", link);
                    String key = chatId + "_" + tag;
                    Objects.requireNonNull(cacheManager.getCache("linksByTag")).evict(key);
                })
                .doOnError(e -> log.error("Error deleting tag for link {}", link));
    }

    /**
     * Получает список ссылок по указанному тегу с кэшированием в "linksByTag".
     *
     * @param request объект запроса с chatId и тегом
     * @return Mono со списком ссылок
     */
    @Cacheable(cacheNames = "linksByTag", key = "#request.chatId + '_' + #request.tag")
    public Mono<ListLinksResponse> getLinksByTag(TagRequest request) {
        return client.getLinksByTag(request)
                .doOnSuccess(r -> log.info("Fetched links by tag '{}' for chat {}", request.tag(), request.chatId()))
                .doOnError(e ->
                        log.error("Error fetching links by tag '{}' for chat {}", request.tag(), request.chatId()));
    }

    /**
     * Удаляет все ссылки по указанному тегу и инвалидирует кеш "linksByTag".
     *
     * @param chatId идентификатор Telegram-чата
     * @param tag тег, по которому удаляются ссылки
     * @return Mono<Void> после выполнения операции
     */
    public Mono<Void> deleteLinksByTag(long chatId, String tag) {
        return client.deleteLinksByTag(chatId, tag)
                .doOnSuccess(__ -> {
                    log.info("Deleted links by tag '{}' for chat {}", tag, chatId);
                    String key = chatId + "_" + tag;
                    Objects.requireNonNull(cacheManager.getCache("linksByTag")).evict(key);
                })
                .doOnError(e -> log.error("Error deleting links by tag '{}' for chat {}", tag, chatId));
    }
}
