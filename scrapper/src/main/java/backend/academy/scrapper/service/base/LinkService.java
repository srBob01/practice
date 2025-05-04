package backend.academy.scrapper.service.base;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.scrapper.model.db.link.Link;
import java.util.List;
import java.util.Optional;

public interface LinkService {
    /**
     * Возвращает и «резервирует» пакет ссылок для последующей проверки обновлений.
     *
     * @param intervalSeconds минимальное время с момента последней проверки
     * @param limit максимально число ссылок в пакете
     * @return список сущностей Link для обработки
     */
    List<Link> fetchBatchToUpdate(int intervalSeconds, int limit);

    /**
     * Добавляет новую ссылку (если ещё нет) и возвращает DTO с её данными и тегами.
     *
     * @param request DTO с URL и списком тегов
     * @return DTO с идентификатором, URL и тегами
     */
    LinkResponse addLink(AddLinkRequest request);

    /**
     * Пытается найти ссылку по оригинальному URL.
     *
     * @param url строка URL
     * @return Optional с идентификатором, если ссылка найдена
     */
    Optional<Long> findByUrl(String url);

    /**
     * Обновляет время последнего изменения для указанной сущности Link.
     *
     * @param link сущность Link с новым значением поля lastModified
     */
    void updateLastModified(Link link);
}
