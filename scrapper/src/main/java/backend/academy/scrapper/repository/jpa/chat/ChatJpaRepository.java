package backend.academy.scrapper.repository.jpa.chat;

import backend.academy.scrapper.model.db.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatJpaRepository extends JpaRepository<Chat, Long> {}
