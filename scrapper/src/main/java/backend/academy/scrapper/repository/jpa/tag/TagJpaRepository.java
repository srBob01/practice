package backend.academy.scrapper.repository.jpa.tag;

import backend.academy.scrapper.model.db.tag.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagJpaRepository extends JpaRepository<Tag, Long> {

    @Query(
            value =
                    """
        WITH inserted AS (
            INSERT INTO tag (name)
            VALUES (:name)
            ON CONFLICT (name) DO NOTHING
            RETURNING id
        )
        SELECT id FROM inserted
        UNION
        SELECT id FROM tag WHERE name = :name
        """,
            nativeQuery = true)
    Long insertOrGetIdByName(@Param("name") String name);

    Optional<Tag> findByName(String name);
}
