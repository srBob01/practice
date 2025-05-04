package backend.academy.scrapper.service.base;

import java.util.Optional;

public interface TagService {

    /**
     * Возвращает идентификатор тега с указанным именем. Если тега ещё нет — создаёт его.
     *
     * @param name имя тега, не пустое
     * @return идентификатор существующего или только что созданного тега
     */
    Long getOrCreateTagId(String name);

    /**
     * Пытается найти идентификатор тега по его имени.
     *
     * @param name имя тега для поиска
     * @return Optional с идентификатором тега, или пустой, если такого тега нет
     */
    Optional<Long> getTagIdByName(String name);
}
