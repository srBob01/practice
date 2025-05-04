package backend.academy.scrapper.service.base.impl.jpa;

import backend.academy.scrapper.model.db.outbox.OutboxMessage;
import backend.academy.scrapper.repository.jpa.outbox.OutboxJpaRepository;
import backend.academy.scrapper.service.base.OutboxService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@ConditionalOnProperty(name = "access-type", havingValue = "ORM")
public class OutboxServiceJpaImpl implements OutboxService {

    private final OutboxJpaRepository repository;

    @Override
    public OutboxMessage save(OutboxMessage message) {
        return repository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxMessage> findUnprocessed(int limit) {
        return repository.findUnprocessed(PageRequest.of(0, limit));
    }

    @Override
    public void markProcessed(Long id) {
        repository.markProcessedById(id);
    }
}
