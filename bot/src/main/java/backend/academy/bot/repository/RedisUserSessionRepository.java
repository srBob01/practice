package backend.academy.bot.repository;

import backend.academy.bot.entity.UserSession;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для хранения сессий пользователей в Redis.
 *
 * <p>Использует строковые ключи формата "session:{chatId}" для операций GET/SET, а также позволяет получать все
 * активные сессии путём сканирования ключей.
 */
@Repository
@RequiredArgsConstructor
public class RedisUserSessionRepository {

    private static final String KEY_PREFIX = "session:";

    private final RedisTemplate<String, UserSession> redisTpl;

    /**
     * Строит Redis-ключ для заданного chatId.
     *
     * @param chatId идентификатор чата пользователя
     * @return строка ключа для Redis
     */
    private String key(Long chatId) {
        return KEY_PREFIX + chatId;
    }

    /**
     * Получает сессию пользователя по chatId.
     *
     * @param chatId идентификатор чата
     * @return объект UserSession или null, если отсутствует
     */
    public UserSession get(Long chatId) {
        return redisTpl.opsForValue().get(key(chatId));
    }

    /**
     * Сохраняет или обновляет сессию пользователя в Redis.
     *
     * @param chatId идентификатор чата
     * @param session объект сессии пользователя
     */
    public void save(Long chatId, UserSession session) {
        redisTpl.opsForValue().set(key(chatId), session);
    }

    /**
     * Удаляет сессию пользователя по chatId.
     *
     * @param chatId идентификатор чата
     */
    public void remove(long chatId) {
        redisTpl.delete(key(chatId));
    }

    /**
     * Получает несколько сессий по набору chatId.
     *
     * @param chatIds набор идентификаторов чатов
     * @return карта из chatId в UserSession для найденных сессий
     */
    public Map<Long, UserSession> multiGet(Set<Long> chatIds) {
        List<String> keys = chatIds.stream().map(this::key).toList();
        List<UserSession> sessions = redisTpl.opsForValue().multiGet(keys);
        Map<Long, UserSession> map = new HashMap<>();
        int i = 0;
        for (Long id : chatIds) {
            assert sessions != null;
            UserSession s = sessions.get(i++);
            if (s != null) {
                map.put(id, s);
            }
        }
        return map;
    }

    /**
     * Получает все пользовательские сессии, сканируя ключи в Redis.
     *
     * @return карта chatId → UserSession для всех активных сессий
     */
    public Map<Long, UserSession> findAllSessions() {
        // 1) Сканируем ключи
        Set<String> keySet = new HashSet<>();
        redisTpl.execute((RedisCallback<Void>) conn -> {
            ScanOptions opts =
                    ScanOptions.scanOptions().match(KEY_PREFIX + "*").count(500).build();
            try (Cursor<byte[]> cursor = conn.keyCommands().scan(opts)) {
                while (cursor.hasNext()) {
                    keySet.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return null;
        });

        if (keySet.isEmpty()) {
            return new HashMap<>();
        }

        // 2) Пакетно достаём все сессии
        List<UserSession> sessions = redisTpl.opsForValue().multiGet(new ArrayList<>(keySet));
        Map<Long, UserSession> result = new HashMap<>();
        int i = 0;
        for (String k : keySet) {
            assert sessions != null;
            UserSession session = sessions.get(i++);
            if (session != null) {
                Long chatId = Long.parseLong(k.substring(KEY_PREFIX.length()));
                result.put(chatId, session);
            }
        }
        return result;
    }
}
