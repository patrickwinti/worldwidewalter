package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.repository.GameRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

/**
 * Periodically hands the host role of a not-yet-started game to a random remaining player when
 * the current host has been gone longer than the disconnect grace period. This is the time-based
 * counterpart to the immediate reassignment that happens when a host leaves gracefully.
 */
@Service
public class LobbyMaintenanceService {
    private final Logger logger = Logger.getLogger(LobbyMaintenanceService.class.getSimpleName());

    private final GameRepository repository;
    private final GameService gameService;

    public LobbyMaintenanceService(GameRepository repository, GameService gameService) {
        this.repository = repository;
        this.gameService = gameService;
    }

    @Scheduled(fixedDelayString = "${lobby.host-check.interval}", initialDelayString = "${lobby.host-check.interval}")
    protected void reassignAbsentHosts() {
        List<String> lobbyGameIds;
        synchronized (repository) {
            lobbyGameIds = StreamSupport.stream(repository.findAll().spliterator(), false)
                    .filter(game -> !game.isStarted())
                    .map(Game::getId)
                    .toList();
        }
        for (String gameId : lobbyGameIds) {
            try {
                gameService.reassignHostIfAbsent(gameId);
            } catch (GameError.NotFoundException e) {
                logger.fine(() -> "Lobby game " + gameId + " removed before host check");
            }
        }
    }
}
