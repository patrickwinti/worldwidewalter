package ch.zhaw.www.websocket;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the mapping from WebSocket session IDs to game/player info.
 */
@Component
public class PlayerSessionRegistry {

    private record PlayerSession(String gameId, String playerId) {}

    private final Map<String, PlayerSession> sessions = new ConcurrentHashMap<>();

    public void register(String sessionId, String gameId, String playerId) {
        sessions.put(sessionId, new PlayerSession(gameId, playerId));
    }

    public Optional<String[]> remove(String sessionId) {
        PlayerSession session = sessions.remove(sessionId);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.of(new String[]{session.gameId(), session.playerId()});
    }
}
