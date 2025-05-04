package backend.academy.scrapper.service.base.impl.jdbc;

import backend.academy.scrapper.repository.jdbc.TagJdbcRepository;
import backend.academy.scrapper.service.base.TagService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class TagServiceJdbcImpl implements TagService {
    private final TagJdbcRepository tagJdbcRepository;

    @Override
    public Long getOrCreateTagId(String name) {
        return tagJdbcRepository.insertOrGetIdByName(name);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Long> getTagIdByName(String name) {
        return tagJdbcRepository.getIdByName(name);
    }
}
