package backend.academy.scrapper.repository.jpa.link;

import backend.academy.scrapper.model.db.link.Link;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkJpaRepository extends JpaRepository<Link, Long> {
    Optional<Link> findLinkByOriginalUrl(String originalUrl);

    @Query(
            value =
                    """
            SELECT id FROM link
            WHERE last_checked < (CURRENT_TIMESTAMP - make_interval(secs := :interval))
            ORDER BY last_checked, id
            LIMIT :limit
            FOR NO KEY UPDATE SKIP LOCKED
        """,
            nativeQuery = true)
    List<Long> fetchIdsToUpdate(@Param("interval") int intervalSeconds, @Param("limit") int limit);
}
