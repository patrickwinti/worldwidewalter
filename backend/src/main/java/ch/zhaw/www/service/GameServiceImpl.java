package ch.zhaw.www.service;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.repository.PromptRepository;
import ch.zhaw.www.utils.RandomProvider;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the GameService interface that interacts with the EntityService and RoundService.
 */
@Service
class GameServiceImpl implements GameService {
    private static final Logger LOGGER = Logger.getLogger(GameService.class.getSimpleName());
    private static final int DEFAULT_NUMBER_OF_ROUNDS = 1;
    
    private final EntityService entityService;
    private final GameProperties gameProperties;
    private final RoundService roundService;
    private final RandomProvider randomProvider;
    
    private final PromptRepository promptRepository;
    
    GameServiceImpl(EntityService entityService, GameProperties gameProperties, RoundService roundService,
                    RandomProvider randomProvider, PromptRepository promptRepository) {
        this.entityService = entityService;
        this.gameProperties = gameProperties;
        this.roundService = roundService;
        this.randomProvider = randomProvider;
        this.promptRepository = promptRepository;
    }
    
    @Override
    public Game createGame() throws GameError.ExistAlready {
        var game = new Game(randomProvider.getEightCharacterId(),
                gameProperties.getMinimumAmountOfActivePlayersPerGame(),
                gameProperties.getMaximumAmountOfActivePlayersPerGame(),
                DEFAULT_NUMBER_OF_ROUNDS,
                promptRepository.getPrompts());
        entityService.saveNewGame(game);
        return game;
    }
    
    @Override
    public Game getGame(@NotNull String gameId) throws GameError.NotFoundException {
        return entityService.getGame(gameId);
    }
    
    @Override
    public Player enterGame(@NotNull String gameId, @NotNull String playerName) throws GameError.NotFoundException, GameError.FullCapacityException {
        return entityService.editGame(gameId, game -> {
            String uuid = UUID.randomUUID().toString();
            StringBuilder name = new StringBuilder(playerName.trim());
            while (game.getAllPlayers().anyMatch(player -> name.toString().equals(player.getName()))) {
                name.append(randomProvider.getPostfix());
            }
            Player tempPlayer = new Player(uuid, name.toString());
            game.registerPlayer(tempPlayer);
            return tempPlayer;
        });
    }
    
    @Override
    public void leaveGame(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException {
        entityService.editGame(gameId, game -> {
            checkPlayerInGame(game, playerId);
            game.removePlayer(playerId);
            return game;
        });
    }
    
    @Override
    public void enterRound(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException, PlayerError.NotFoundException {
        entityService.editGame(gameId, game -> {
            Player playerEnteringRound = checkPlayerInGame(game, playerId);
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
        return getCurrentRoundForDesiredState(gameId, playerId, Game::canAcceptPropositions);
    }
    
    @Override
    public Round getRoundClosedForSelections(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException {
        return getCurrentRoundForDesiredState(gameId, playerId, Game::needsNewRound);
    }
    
    private Round getCurrentRoundForDesiredState(final String gameId, final String playerId, Predicate<Game> predicate) {
        Game game = entityService.getGame(gameId);
        checkPlayerInGame(game, playerId);
        if (!game.hasActivePlayer(playerId) || !predicate.test(game)) {
            LOGGER.info(() -> "Game not in desired state");
            throw new RoundError.IllegalStateException();
        }
        return game.getCurrentRound();
    }
    
    private static Player checkPlayerInGame(final Game game, final String playerId) {
        return game.getAllPlayers()
                .filter(p -> Objects.equals(p.getId(), playerId)).findFirst()
                .orElseThrow(() -> new PlayerError.NotFoundException(playerId));
    }
}
