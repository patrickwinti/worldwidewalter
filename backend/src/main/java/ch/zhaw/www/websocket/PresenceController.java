package ch.zhaw.www.websocket;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles STOMP messages for player presence registration.
 */
@Controller
public class PresenceController {

    private static final Logger LOGGER = Logger.getLogger(PresenceController.class.getSimpleName());

    private final PlayerSessionRegistry registry;

    PresenceController(PlayerSessionRegistry registry) {
        this.registry = registry;
    }

    @MessageMapping("/presence/register")
    public void registerPresence(
            @Header("simpSessionId") String sessionId,
            @Header("gameId") String gameId,
            @Header("playerId") String playerId) {
        registry.register(sessionId, gameId, playerId);
        LOGGER.log(Level.INFO, "Registered session {0} for player {1} in game {2}",
                new Object[]{sessionId, playerId, gameId});
    }
}
