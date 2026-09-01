package ch.zhaw.www.service;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.repository.PromptRepository;
import ch.zhaw.www.utils.RandomProvider;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the GameService interface that interacts with the EntityService and RoundService.
 */
@Service
class GameServiceImpl implements GameService {
    private static final Logger LOGGER = Logger.getLogger(GameService.class.getSimpleName());
    private static final int DEFAULT_NUMBER_OF_ROUNDS = 1;
    private static final int MAX_ROOM_CODE_ATTEMPTS = 20;
    
    private final EntityService entityService;
    private final GameProperties gameProperties;
    private final RoundService roundService;
    private final RandomProvider randomProvider;
    private final GameNotifier gameNotifier;

    private final PromptRepository promptRepository;

    GameServiceImpl(EntityService entityService, GameProperties gameProperties, RoundService roundService,
                    RandomProvider randomProvider, GameNotifier gameNotifier, PromptRepository promptRepository) {
        this.entityService = entityService;
        this.gameProperties = gameProperties;
        this.roundService = roundService;
        this.randomProvider = randomProvider;
        this.gameNotifier = gameNotifier;
        this.promptRepository = promptRepository;
    }
    
    @Override
    public Game createGame(@NotNull String hostName) throws GameError.ExistAlready {
        Player host = new Player(UUID.randomUUID().toString(), hostName.trim());
        for (int attempt = 0; attempt < MAX_ROOM_CODE_ATTEMPTS; attempt++) {
            var game = new Game(randomProvider.getRoomCode(),
                    gameProperties.getMinimumAmountOfActivePlayersPerGame(),
                    gameProperties.getMaximumAmountOfActivePlayersPerGame(),
                    DEFAULT_NUMBER_OF_ROUNDS,
                    promptRepository.getPrompts());
            game.registerPlayer(host);
            game.setHostId(host.getId());
            try {
                entityService.saveNewGame(game);
                return game;
            } catch (GameError.ExistAlready e) {
                LOGGER.log(Level.INFO, "Room code {0} already in use, retrying", game.getId());
            }
        }
        throw new GameError.ExistAlready();
    }

    @Override
    public Game startGame(@NotNull String gameId, @NotNull String playerId)
            throws GameError.NotFoundException, GameError.NotHostException, GameError.NotEnoughPlayersException {
        Game game = entityService.editGame(gameId, g -> {
            requireHost(g, playerId);
            if (!g.hasEnoughPlayersToStart()) {
                throw new GameError.NotEnoughPlayersException();
            }
            g.markStarted();
            LOGGER.log(Level.INFO, "Game {0} started by host {1}", new Object[]{gameId, playerId});
            return g;
        });
        gameNotifier.notifyLobbyChanged(game);
        return game;
    }

    @Override
    public Game endGame(@NotNull String gameId, @NotNull String playerId)
            throws GameError.NotFoundException, GameError.NotHostException {
        Game game = entityService.editGame(gameId, g -> {
            requireHost(g, playerId);
            g.markEnded();
            LOGGER.log(Level.INFO, "Game {0} ended by host {1}", new Object[]{gameId, playerId});
            return g;
        });
        gameNotifier.notifyRoundChanged(game);
        return game;
    }

    @Override
    public Game restartGame(@NotNull String gameId, @NotNull String playerId)
            throws GameError.NotFoundException, GameError.NotHostException {
        Game game = entityService.editGame(gameId, g -> {
            requireHost(g, playerId);
            g.restart();
            LOGGER.log(Level.INFO, "Game {0} restarted by host {1}", new Object[]{gameId, playerId});
            return g;
        });
        gameNotifier.notifyLobbyChanged(game);
        return game;
    }

    @Override
    public void reassignHostIfAbsent(@NotNull String gameId) {
        // Read first so untouched games do not have their lastEdit bumped (which would defeat idle cleanup).
        Game current = entityService.getGame(gameId);
        if (current.isStarted() || current.isHostPresent() || current.getPresentPlayers().isEmpty()) {
            return;
        }
        Game game = entityService.editGame(gameId, g -> {
            reassignHost(g);
            return g;
        });
        gameNotifier.notifyLobbyChanged(game);
    }

    /**
     * Picks a random present player as the new host, unless the host became present again in the
     * meantime or nobody is left to promote.
     *
     * @param game the game to reassign the host for
     */
    private void reassignHost(Game game) {
        if (game.isStarted() || game.isHostPresent()) {
            return;
        }
        List<Player> candidates = game.getPresentPlayers();
        if (candidates.isEmpty()) {
            return;
        }
        Player newHost = candidates.get(randomProvider.getRandomIndex(candidates.size()));
        game.setHostId(newHost.getId());
        LOGGER.log(Level.INFO, "Host of game {0} reassigned to {1}", new Object[]{game.getId(), newHost.getId()});
    }
    
    @Override
    public Game getGame(@NotNull String gameId) throws GameError.NotFoundException {
        return entityService.getGame(gameId);
    }
    
    @Override
    public Player enterGame(@NotNull String gameId, @NotNull String playerName) throws GameError.NotFoundException, GameError.FullCapacityException {
        Player[] joined = new Player[1];
        Game game = entityService.editGame(gameId, g -> {
            String name = playerName.trim();
            // Names identify players in the round and in the results, so a duplicate is
            // rejected and the player picks another one instead of being silently renamed.
            if (g.getAllPlayers().anyMatch(player -> name.equalsIgnoreCase(player.getName()))) {
                throw new GameError.NameTakenException(name);
            }
            Player tempPlayer = new Player(UUID.randomUUID().toString(), name);
            g.registerPlayer(tempPlayer);
            joined[0] = tempPlayer;
            return g;
        });
        gameNotifier.notifyLobbyChanged(game);
        return joined[0];
    }

    @Override
    public void leaveGame(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException {
        Game game = entityService.editGame(gameId, g -> {
            checkPlayerInGame(g, playerId);
            g.removePlayer(playerId);
            // If the host left, hand the host role to a random remaining present player.
            reassignHost(g);
            return g;
        });
        gameNotifier.notifyLobbyChanged(game);
    }

    @Override
    public void enterRound(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException, PlayerError.NotFoundException {
        entityService.editGame(gameId, game -> {
            Player playerEnteringRound = checkPlayerInGame(game, playerId);
            if (!game.isStarted()) {
                throw new GameError.NotStartedException();
            }
            if (game.isEnded()) {
                throw new GameError.EndedException();
            }
            if (game.needsNewRound()) {
                var round = roundService.createNewRound(game);
                game.addRound(round);
                game.moveToActivePlayers(playerEnteringRound);
                LOGGER.log(Level.INFO, "Creating a new round for game {0}", gameId);
            } else if (game.canRoundBeEntered()) {
                game.moveToActivePlayers(playerEnteringRound);
                roundService.selectSphinx(game);
                LOGGER.log(Level.INFO, "Adding player to round {0}", gameId);
            } else if (!game.hasCapacityForNewActivePlayer()) {
                LOGGER.info("Round is full, player cannot join");
                throw new GameError.FullCapacityException();
            } else {
                LOGGER.info("Player cannot join current session");
                throw new RoundError.IllegalOperationException();
            }
            return game;
        });
        gameNotifier.notifyRoundChanged(entityService.getGame(gameId));
    }
    
    @Override
    public Round getRoundOpenForPropositions(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException {
        Game game = startRoundIfWaitElapsed(gameId);
        checkPlayerInGame(game, playerId);
        if (!game.hasActivePlayer(playerId) || !game.canAcceptPropositionsForCurrentRound()) {
            LOGGER.info(() -> "Game not in desired state");
            throw new RoundError.IllegalStateException();
        }
        return game.getCurrentRound();
    }
    
    /**
     * Starts a round that is only still waiting for players who never continued, once the wait
     * for them has elapsed. Clients enter a round once and then poll for it, so without this the
     * backstop would never be reached: a single player idling on the results screen would keep
     * everybody else waiting forever.
     * <p>
     * The game is read first and only written when the round actually starts, so that polling
     * does not keep an abandoned game alive past the idle cleanup.
     *
     * @param gameId game identifier
     * @return the game, with the round started if it was due
     */
    private Game startRoundIfWaitElapsed(@NotNull String gameId) {
        Game game = entityService.getGame(gameId);
        boolean roundWaitingToStart = game.isStarted()
                && game.getCurrentRoundOptional().map(round -> round.getSphinx() == null).orElse(false)
                && game.allExpectedPlayersEntered();
        if (!roundWaitingToStart) {
            return game;
        }
        Game started = entityService.editGame(gameId, g -> {
            roundService.selectSphinx(g);
            return g;
        });
        gameNotifier.notifyRoundChanged(started);
        return started;
    }

    @Override
    public void disconnectPlayer(@NotNull String gameId, @NotNull String playerId) {
        try {
            Game game = entityService.editGame(gameId, g -> {
                g.markPlayerDisconnected(playerId);
                return g;
            });
            notifyPresenceChanged(game);
        } catch (GameError.NotFoundException e) {
            LOGGER.log(Level.WARNING, "Game {0} not found on player disconnect", gameId);
        }
    }

    @Override
    public void connectPlayer(@NotNull String gameId, @NotNull String playerId) {
        try {
            Game game = entityService.editGame(gameId, g -> {
                g.markPlayerConnected(playerId);
                return g;
            });
            notifyPresenceChanged(game);
        } catch (GameError.NotFoundException e) {
            LOGGER.log(Level.WARNING, "Game {0} not found on player connect", gameId);
        }
    }

    /**
     * Pushes a presence change to whichever view the game is currently in, so a player who
     * drops out stops being an unexplained blank in the lobby list or in the round wait.
     *
     * @param game the game whose presence changed
     */
    private void notifyPresenceChanged(Game game) {
        if (game.isStarted()) {
            gameNotifier.notifyRoundChanged(game);
        } else {
            gameNotifier.notifyLobbyChanged(game);
        }
    }

    /**
     * @param game     the game to check
     * @param playerId the player that must be the host
     * @throws GameError.NotHostException if the player is not the host of the game
     */
    private static void requireHost(Game game, String playerId) {
        if (!game.isHost(playerId)) {
            throw new GameError.NotHostException();
        }
    }

    @Override
    public Player rejoinGame(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException, PlayerError.NotFoundException {
        return entityService.editGame(gameId, game -> {
            Player player = checkPlayerInGame(game, playerId);
            game.markPlayerConnected(playerId);
            LOGGER.log(Level.INFO, "Player {0} rejoined game {1}", new Object[]{playerId, gameId});
            return player;
        });
    }

    private static Player checkPlayerInGame(final Game game, final String playerId) {
        return game.getAllPlayers()
                .filter(p -> Objects.equals(p.getId(), playerId)).findFirst()
                .orElseThrow(() -> new PlayerError.NotFoundException(playerId));
    }
}
