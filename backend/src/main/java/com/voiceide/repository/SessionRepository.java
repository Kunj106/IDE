package com.voiceide.repository;

import com.voiceide.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
    List<Session> findByIsActiveTrue();
    Optional<Session> findByShareCode(String shareCode);
}
