package com.voiceide.repository;

import com.voiceide.model.ConversationTurn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversationTurnRepository extends JpaRepository<ConversationTurn, String> {
    List<ConversationTurn> findBySessionIdOrderByTimestampAsc(String sessionId);
    List<ConversationTurn> findBySessionIdIsNullOrderByTimestampAsc();
}
