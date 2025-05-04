package backend.academy.bot.service.base.impl;

import backend.academy.bot.entity.State;
import backend.academy.bot.entity.UserSession;
import backend.academy.bot.repository.RedisUserSessionRepository;
import backend.academy.bot.service.base.UserSessionService;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Реализация сервиса работы с сессиями пользователей через Redis.
 *
 * <p>Использует {@link RedisUserSessionRepository} для операций сохранения, получения и удаления сессий в Redis-кэше.
 */
@Service
@RequiredArgsConstructor
public class RedisUserSessionService implements UserSessionService {
    private final RedisUserSessionRepository userSessionRepository;

    @Override
    public void createSession(Long chatId) {
        UserSession userSession = new UserSession(State.START);
        save(chatId, userSession);
    }

    @Override
    public UserSession get(Long chatId) {
        return userSessionRepository.get(chatId);
    }

    @Override
    public void save(Long chatId, UserSession session) {
        userSessionRepository.save(chatId, session);
    }

    @Override
    public void remove(Long chatId) {
        userSessionRepository.remove(chatId);
    }

    @Override
    public Map<Long, UserSession> multiGet(Set<Long> chatIds) {
        return userSessionRepository.multiGet(chatIds);
    }

    @Override
    public Map<Long, UserSession> findAllSessions() {
        return userSessionRepository.findAllSessions();
    }
}
