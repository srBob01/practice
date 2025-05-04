package backend.academy.scrapper.service.base.impl.jdbc;

import backend.academy.scrapper.model.db.outbox.OutboxMessage;
import backend.academy.scrapper.repository.jdbc.OutboxJdbcRepository;
import backend.academy.scrapper.service.base.OutboxService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class OutboxServiceJdbcImpl implements OutboxService {
    private final OutboxJdbcRepository repository;

    @Override
    public OutboxMessage save(OutboxMessage message) {
        return repository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxMessage> findUnprocessed(int limit) {
        return repository.findUnprocessed(limit);
    }

    @Override
    @Transactional
    public void markProcessed(Long id) {
        repository.markProcessed(id);
    }
}
