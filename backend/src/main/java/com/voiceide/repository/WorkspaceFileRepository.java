package com.voiceide.repository;

import com.voiceide.model.WorkspaceFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkspaceFileRepository extends JpaRepository<WorkspaceFile, String> {
    List<WorkspaceFile> findBySessionId(String sessionId);
    List<WorkspaceFile> findBySessionIdIsNull();
}
