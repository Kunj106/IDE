package com.voiceide.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CollaborationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // sessionId -> list of connected sessions
    private final Map<String, List<WebSocketSession>> sessionRooms = new ConcurrentHashMap<>();
    // websocket sessionId -> room/collaborator info
    private final Map<String, String> sessionToRoom = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = extractRoomId(session);
        String userId = "User-" + session.getId().substring(0, 6);

        sessionRooms.computeIfAbsent(roomId, k -> Collections.synchronizedList(new ArrayList<>())).add(session);
        sessionToRoom.put(session.getId(), roomId);
        sessionToUser.put(session.getId(), userId);

        Map<String, Object> welcomeMsg = new HashMap<>();
        welcomeMsg.put("type", "connected");
        welcomeMsg.put("userId", userId);
        welcomeMsg.put("roomId", roomId);
        welcomeMsg.put("participantCount", sessionRooms.get(roomId).size());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMsg)));

        broadcastToRoom(roomId, Map.of(
            "type", "participant_joined",
            "userId", userId,
            "participantCount", sessionRooms.get(roomId).size()
        ), session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = sessionToRoom.get(session.getId());
        String userId = sessionToUser.get(session.getId());

        Map<?, ?> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");

        Map<String, Object> broadcast = new HashMap<>(Map.of(
            "type", type != null ? type : "code_change",
            "userId", userId,
            "payload", payload
        ));

        switch (type != null ? type : "") {
            case "cursor_move" -> broadcast.put("cursor", payload.get("cursor"));
            case "code_change" -> broadcast.put("delta", payload.get("delta"));
            case "ping" -> {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of("type", "pong"))));
                return;
            }
        }

        broadcastToRoom(roomId, broadcast, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomId = sessionToRoom.remove(session.getId());
        String userId = sessionToUser.remove(session.getId());

        if (roomId != null) {
            List<WebSocketSession> room = sessionRooms.get(roomId);
            if (room != null) {
                room.remove(session);
                if (room.isEmpty()) {
                    sessionRooms.remove(roomId);
                } else {
                    try {
                        broadcastToRoom(roomId, Map.of(
                            "type", "participant_left",
                            "userId", userId != null ? userId : "unknown",
                            "participantCount", room.size()
                        ), null);
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }

    private void broadcastToRoom(String roomId, Map<?, ?> message, String excludeSessionId) throws IOException {
        List<WebSocketSession> room = sessionRooms.get(roomId);
        if (room == null) return;

        String json = objectMapper.writeValueAsString(message);
        synchronized (room) {
            for (WebSocketSession s : room) {
                if (s.isOpen() && (excludeSessionId == null || !s.getId().equals(excludeSessionId))) {
                    s.sendMessage(new TextMessage(json));
                }
            }
        }
    }

    private String extractRoomId(WebSocketSession session) {
        String path = session.getUri() != null ? session.getUri().getPath() : "";
        String[] parts = path.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "default";
    }
}
