package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.repository.GameRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Keeps a waiting lobby usable while nobody is interacting with it:
 * <ul>
 *     <li>hands the host role to a random remaining player once the current host has been gone
 *     longer than the disconnect grace period (the time-based counterpart to the immediate
 *     reassignment when a host leaves gracefully),</li>
 *     <li>broadcasts the lobby again when the set of present players changed, because a player
 *     dropping out only becomes visible when their grace period elapses and no request or
 *     WebSocket event happens at that moment.</li>
 * </ul>
 */
@Service
public class LobbyMaintenanceService {
    private final Logger logger = Logger.getLogger(LobbyMaintenanceService.class.getSimpleName());

    private final GameRepository repository;
    private final GameService gameService;
    private final GameNotifier gameNotifier;
    /** Present players per lobby as last broadcast, used to detect changes. */
    private final Map<String, Set<String>> lastKnownPresence = new ConcurrentHashMap<>();

    public LobbyMaintenanceService(GameRepository repository, GameService gameService, GameNotifier gameNotifier) {
        this.repository = repository;
        this.gameService = gameService;
        this.gameNotifier = gameNotifier;
    }

    @Scheduled(fixedDelayString = "${lobby.host-check.interval}", initialDelayString = "${lobby.host-check.interval}")
    protected void maintainLobbies() {
        List<String> lobbyGameIds;
        synchronized (repository) {
            lobbyGameIds = StreamSupport.stream(repository.findAll().spliterator(), false)
                    .filter(game -> !game.isStarted())
                    .map(Game::getId)
                    .toList();
        }
        lastKnownPresence.keySet().retainAll(lobbyGameIds);
        for (String gameId : lobbyGameIds) {
            try {
                gameService.reassignHostIfAbsent(gameId);
                broadcastIfPresenceChanged(gameId);
            } catch (GameError.NotFoundException e) {
                logger.fine(() -> "Lobby game " + gameId + " removed before host check");
            }
        }
    }

    /**
     * Broadcasts the lobby of the given game if the players present in it changed since the
     * last check.
     *
     * @param gameId game identifier
     */
    private void broadcastIfPresenceChanged(String gameId) {
        Game game = gameService.getGame(gameId);
        Set<String> present = game.getPresentPlayers().stream()
                .map(Player::getId)
                .collect(Collectors.toSet());
        Set<String> previous = lastKnownPresence.put(gameId, present);
        if (!present.equals(previous)) {
            gameNotifier.notifyLobbyChanged(game);
        }
    }
}
