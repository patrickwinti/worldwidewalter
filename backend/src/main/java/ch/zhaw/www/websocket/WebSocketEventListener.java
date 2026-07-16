package ch.zhaw.www.websocket;

import ch.zhaw.www.service.GameService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listens for WebSocket disconnect events and marks players as disconnected.
 */
@Component
public class WebSocketEventListener {

    private static final Logger LOGGER = Logger.getLogger(WebSocketEventListener.class.getSimpleName());

    private final PlayerSessionRegistry registry;
    private final GameService gameService;

    WebSocketEventListener(PlayerSessionRegistry registry, GameService gameService) {
        this.registry = registry;
        this.gameService = gameService;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        registry.remove(sessionId).ifPresent(info -> {
            String gameId = info[0];
            String playerId = info[1];
            LOGGER.log(Level.INFO, "Player {0} disconnected from game {1}", new Object[]{playerId, gameId});
            gameService.disconnectPlayer(gameId, playerId);
        });
    }
}
