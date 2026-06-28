package com.voiceide.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_turns")
@Data
@NoArgsConstructor
public class ConversationTurn {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column
    private LocalDateTime timestamp;

    @Column(name = "audio_available")
    private boolean audioAvailable = false;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }
}
