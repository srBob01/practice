package backend.academy.bot.repository;

import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с упорядоченными множествами (ZSet) в Redis, используемый для хранения дайджестов уведомлений.
 */
@Repository
@RequiredArgsConstructor
public class RedisDigestRepository {
    private final RedisTemplate<String, Object> redisTpl;

    /**
     * Добавляет элемент в упорядоченное множество по указанному ключу.
     *
     * @param key ключ ZSet в Redis
     * @param value значение, сохраняемое в множестве
     * @param score оценка для обеспечения сортировки
     */
    public void zAdd(String key, String value, double score) {
        redisTpl.opsForZSet().add(key, value, score);
    }

    /**
     * Возвращает все элементы из упорядоченного множества по ключу.
     *
     * @param key ключ ZSet в Redis
     * @return множество элементов в порядке возрастания их оценки
     */
    public Set<Object> zRange(String key) {
        return redisTpl.opsForZSet().range(key, 0, -1);
    }

    /**
     * Удаляет заданный ключ и все связанные значения из Redis.
     *
     * @param key ключ для удаления
     */
    public void delete(String key) {
        redisTpl.delete(key);
    }

    /**
     * Возвращает остаточное время жизни ключа в секундах.
     *
     * @param key ключ для проверки
     * @return время жизни в секундах или -2, если ключ не существует
     */
    public Long getExpire(String key) {
        return redisTpl.getExpire(key);
    }

    /**
     * Устанавливает время жизни (TTL) для заданного ключа.
     *
     * @param key ключ, для которого устанавливается TTL
     * @param ttl желаемая длительность жизни ключа
     */
    public void expire(String key, Duration ttl) {
        redisTpl.expire(key, ttl);
    }
}
