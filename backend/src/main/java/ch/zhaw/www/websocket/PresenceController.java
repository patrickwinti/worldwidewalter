package ch.zhaw.www.websocket;

import ch.zhaw.www.service.GameService;
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
    private final GameService gameService;

    PresenceController(PlayerSessionRegistry registry, GameService gameService) {
        this.registry = registry;
        this.gameService = gameService;
    }

    @MessageMapping("/presence/register")
    public void registerPresence(
            @Header("simpSessionId") String sessionId,
            @Header("gameId") String gameId,
            @Header("playerId") String playerId) {
        registry.register(sessionId, gameId, playerId);
        // A (re)connecting socket clears any pending disconnect grace timer for the player.
        gameService.connectPlayer(gameId, playerId);
        LOGGER.log(Level.INFO, "Registered session {0} for player {1} in game {2}",
                new Object[]{sessionId, playerId, gameId});
    }
}
