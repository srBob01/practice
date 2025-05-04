package backend.academy.bot.service.notification.impl;

import backend.academy.bot.repository.RedisDigestRepository;
import backend.academy.bot.service.notification.DigestService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Реализация {@link DigestService}, основанная на хранилище Redis.
 *
 * <p>Накопленные уведомления сохраняются в упорядоченном множестве Redis с TTL, заданным константой DIGEST_TTL. При
 * выгрузке все элементы возвращаются и удаляются.
 */
@Service
@RequiredArgsConstructor
public class RedisDigestService implements DigestService {
    private static final Duration DIGEST_TTL = Duration.ofHours(25);
    private final RedisDigestRepository repo;

    private String key(long chatId) {
        return "digest:" + chatId;
    }

    @Override
    public void addUpdate(long chatId, String formatted) {
        String k = key(chatId);
        double score = System.currentTimeMillis();
        repo.zAdd(k, formatted, score);

        Long ttl = repo.getExpire(k);
        if (ttl == null || ttl < 0) {
            repo.expire(k, DIGEST_TTL);
        }
    }

    @Override
    public List<String> fetchAndClear(long chatId) {
        String k = key(chatId);
        Set<Object> entries = repo.zRange(k);
        repo.delete(k);

        List<String> out = new ArrayList<>();
        if (entries != null) {
            for (Object o : entries) {
                out.add(o.toString());
            }
        }
        return out;
    }
}
