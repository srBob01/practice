package backend.academy.scrapper.service.base.impl.jpa;

import backend.academy.scrapper.model.db.tag.Tag;
import backend.academy.scrapper.repository.jpa.tag.TagJpaRepository;
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
@ConditionalOnProperty(name = "access-type", havingValue = "ORM")
public class TagServiceJpaImpl implements TagService {
    private final TagJpaRepository tagJpaRepository;

    @Override
    public Long getOrCreateTagId(String name) {
        return tagJpaRepository.insertOrGetIdByName(name);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Long> getTagIdByName(String name) {
        return tagJpaRepository.findByName(name).map(Tag::id);
    }
}
