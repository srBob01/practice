package backend.academy.scrapper.service.base.impl.jdbc.enrich.helper;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.repository.jdbc.link.impl.LinkImplRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Базовая реализация {@link LinkDetailsFetcher}, которая общими шагами:
 *
 * <ul>
 *   <li>Кастит входной список к нужному типу T;
 *   <li>Собирает их идентификаторы;
 *   <li>Вызывает {@link LinkImplRepository#findByIds(List, Map)} для массовой загрузки;
 *   <li>Возвращает исходный список со заполненными данными.
 * </ul>
 *
 * @param <T> конкретный подкласс {@link Link}
 */
public abstract class AbstractLinkDetailsFetcher<T extends Link> implements LinkDetailsFetcher {
    private final LinkImplRepository<T> repository;

    protected AbstractLinkDetailsFetcher(LinkImplRepository<T> repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Приводит базовый список к T, собирает их id, заполняет подробности через репозиторий.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Link> fetchDetails(List<Link> baseLinks) {
        List<T> typed = (List<T>) baseLinks;
        List<Long> ids = typed.stream().map(Link::id).toList();
        Map<Long, T> map = typed.stream().collect(Collectors.toMap(Link::id, l -> l));
        repository.findByIds(ids, map);
        return baseLinks;
    }
}
