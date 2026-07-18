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
    private final LobbyNotifier lobbyNotifier;

    private final PromptRepository promptRepository;

    GameServiceImpl(EntityService entityService, GameProperties gameProperties, RoundService roundService,
                    RandomProvider randomProvider, LobbyNotifier lobbyNotifier, PromptRepository promptRepository) {
        this.entityService = entityService;
        this.gameProperties = gameProperties;
        this.roundService = roundService;
        this.randomProvider = randomProvider;
        this.lobbyNotifier = lobbyNotifier;
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
            if (!g.isHost(playerId)) {
                throw new GameError.NotHostException();
            }
            if (!g.hasEnoughPlayersToStart()) {
                throw new GameError.NotEnoughPlayersException();
            }
            g.markStarted();
            LOGGER.log(Level.INFO, "Game {0} started by host {1}", new Object[]{gameId, playerId});
            return g;
        });
        lobbyNotifier.notifyLobbyChanged(game);
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
        lobbyNotifier.notifyLobbyChanged(game);
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
            String uuid = UUID.randomUUID().toString();
            StringBuilder name = new StringBuilder(playerName.trim());
            while (g.getAllPlayers().anyMatch(player -> name.toString().equals(player.getName()))) {
                name.append(randomProvider.getPostfix());
            }
            Player tempPlayer = new Player(uuid, name.toString());
            g.registerPlayer(tempPlayer);
            joined[0] = tempPlayer;
            return g;
        });
        lobbyNotifier.notifyLobbyChanged(game);
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
        lobbyNotifier.notifyLobbyChanged(game);
    }

    @Override
    public void enterRound(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException, PlayerError.NotFoundException {
        entityService.editGame(gameId, game -> {
            Player playerEnteringRound = checkPlayerInGame(game, playerId);
            if (!game.isStarted()) {
                throw new GameError.NotStartedException();
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
    }
    
    @Override
    public Round getRoundOpenForPropositions(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException {
        Game game = entityService.getGame(gameId);
        checkPlayerInGame(game, playerId);
        if (!game.hasActivePlayer(playerId) || !game.canAcceptPropositionsForCurrentRound()) {
            LOGGER.info(() -> "Game not in desired state");
            throw new RoundError.IllegalStateException();
        }
        return game.getCurrentRound();
    }
    
    @Override
    public void disconnectPlayer(@NotNull String gameId, @NotNull String playerId) {
        try {
            entityService.editGame(gameId, game -> {
                game.markPlayerDisconnected(playerId);
                return game;
            });
        } catch (GameError.NotFoundException e) {
            LOGGER.log(Level.WARNING, "Game {0} not found on player disconnect", gameId);
        }
    }

    @Override
    public void connectPlayer(@NotNull String gameId, @NotNull String playerId) {
        try {
            entityService.editGame(gameId, game -> {
                game.markPlayerConnected(playerId);
                return game;
            });
        } catch (GameError.NotFoundException e) {
            LOGGER.log(Level.WARNING, "Game {0} not found on player connect", gameId);
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
