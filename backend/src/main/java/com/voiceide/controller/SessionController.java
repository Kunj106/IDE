package com.voiceide.controller;

import com.voiceide.dto.SessionInput;
import com.voiceide.model.Session;
import com.voiceide.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<Session>> listSessions() {
        return ResponseEntity.ok(sessionService.listActiveSessions());
    }

    @PostMapping
    public ResponseEntity<Session> createSession(@Valid @RequestBody SessionInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.createSession(input));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Session> getSession(@PathVariable String id) {
        return sessionService.getSession(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable String id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
