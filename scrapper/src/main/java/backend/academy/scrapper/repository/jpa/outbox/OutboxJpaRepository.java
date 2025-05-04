package backend.academy.scrapper.repository.jpa.outbox;

import backend.academy.scrapper.model.db.outbox.OutboxMessage;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxJpaRepository extends JpaRepository<OutboxMessage, Long> {

    @Query("SELECT o FROM OutboxMessage o WHERE o.processedAt IS NULL ORDER BY o.createdAt")
    List<OutboxMessage> findUnprocessed(Pageable pageable);

    @Modifying
    @Query("UPDATE OutboxMessage o SET o.processedAt = CURRENT_TIMESTAMP WHERE o.id = :id")
    int markProcessedById(@Param("id") Long id);
}
