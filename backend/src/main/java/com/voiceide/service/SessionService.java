package com.voiceide.service;

import com.voiceide.dto.SessionInput;
import com.voiceide.model.Session;
import com.voiceide.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    public List<Session> listActiveSessions() {
        return sessionRepository.findByIsActiveTrue();
    }

    public Session createSession(SessionInput input) {
        Session session = new Session();
        session.setId(UUID.randomUUID().toString());
        session.setName(input.getName());
        session.setLanguage(input.getLanguage());
        session.setShareCode(generateShareCode());
        session.setActive(true);
        session.setParticipantCount(1);
        return sessionRepository.save(session);
    }

    public Optional<Session> getSession(String id) {
        return sessionRepository.findById(id);
    }

    public void deleteSession(String id) {
        sessionRepository.findById(id).ifPresent(session -> {
            session.setActive(false);
            sessionRepository.save(session);
        });
    }

    private String generateShareCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String code = sb.toString();
        return sessionRepository.findByShareCode(code).isPresent() ? generateShareCode() : code;
    }
}
