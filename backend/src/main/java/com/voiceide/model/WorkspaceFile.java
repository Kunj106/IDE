package com.voiceide.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "workspace_files")
@Data
@NoArgsConstructor
public class WorkspaceFile {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(nullable = false)
    private String language;

    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Column
    private int size;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastModified = LocalDateTime.now();
        size = content != null ? content.length() : 0;
    }
}
