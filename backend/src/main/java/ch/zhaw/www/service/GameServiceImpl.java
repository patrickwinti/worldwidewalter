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
    public Game getGame(String gameId) throws GameError.NotFoundException {
        return entityService.getGame(gameId);
    }
    
    @Override
    public Player enterGame(String gameId, String playerName) throws GameError.NotFoundException, GameError.FullCapacityException {
        String uuid = UUID.randomUUID().toString();
        var wrapper = new Object() {
            Player player;
        };
        entityService.editGame(gameId, game -> {
            StringBuilder name = new StringBuilder(playerName);
            while (game.getAllPlayers().anyMatch(player -> name.toString().equals(player.getName()))) {
                name.append(randomProvider.getPostfix());
            }
            Player tempPlayer = new Player(uuid, name.toString());
            game.addPlayerToWaitingRoom(tempPlayer);
            wrapper.player = tempPlayer;
        });
        return wrapper.player;
    }
    
    @Override
    public void leaveGame(String gameId, String playerId) throws GameError.NotFoundException {
        entityService.editGame(gameId, game -> {
            checkPlayerInGame(game, playerId);
            game.removePlayer(playerId);
        });
    }
    
    @Override
    public void enterRound(String gameId, String playerId) throws GameError.NotFoundException, PlayerError.NotFoundException {
        entityService.editGame(gameId, game -> {
            Player playerEnteringRound = checkPlayerInGame(game, playerId);
            Game.State state = game.getState();
            switch (state) {
                case NO_VALID_ROUND, WAITING_FOR_NEW_ROUND -> {
                    var round = roundService.createNewRound(game);
                    game.addRound(round);
                    LOGGER.log(Level.INFO, "Creating a new round for game {0}", game);
                    movePlayerToActive(game, playerEnteringRound);
                    game.getAllPlayers()
                            .filter(player -> !game.hasActivePlayer(player.getId()))
                            .takeWhile(player -> game.hasCapacityForNewActivePlayer())
                            .forEach(game::moveToActivePlayers);
                    //handle the case that no other play may enter the game
                    if (!game.hasCapacityForNewActivePlayer()) {
                        roundService.selectSphinx(game);
                    }
                }
                case WAITING_FOR_PLAYERS, WAITING_FOR_ALL_PROPOSITIONS -> {
                    movePlayerToActive(game, playerEnteringRound);
                    LOGGER.log(Level.INFO, "Adding player to round {0}", gameId);
                    roundService.selectSphinx(game);
                }
                case WAITING_FOR_ALL_SELECTIONS -> {
                    //Player can't enter round at the moment. Player will stay in waiting room.
                }
            }
            LOGGER.log(Level.INFO, () -> String.format("Game %s moved to state: %s", gameId, game.getState()));
        });
    }
    
    @Override
    public Round getRoundOpenForPropositions(String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException {
        return getCurrentRoundForDesiredState(gameId, playerId, Game.State.WAITING_FOR_ALL_PROPOSITIONS);
    }
    
    @Override
    public Round getRoundClosedForSelections(String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException {
        return getCurrentRoundForDesiredState(gameId, playerId, Game.State.WAITING_FOR_NEW_ROUND);
    }
    
    private Round getCurrentRoundForDesiredState(final String gameId, final String playerId, final Game.State state) {
        Game game = entityService.getGame(gameId);
        checkPlayerInGame(game, playerId);
        if (game.getState() != state || !game.hasActivePlayer(playerId)) {
            throw new RoundError.IllegalStateException();
        }
        return game.getCurrentRound();
    }
    
    /**
     * Moves the specified player to the active player list of the game if there is capacity for a new active player,
     * otherwise throws a {@link GameError.FullCapacityException}. If the player is not found in the game, a
     * {@link PlayerError.NotFoundException} is thrown.
     *
     * @param game   The game object.
     * @param player The player object to move to the active player list.
     * @throws PlayerError.NotFoundException   If the player is not found in the game.
     * @throws GameError.FullCapacityException If there is no capacity for a new active player in the game.
     */
    private static void movePlayerToActive(final Game game, final Player player) {
        if (game.hasCapacityForNewActivePlayer()) {
            game.moveToActivePlayers(player);
        } else {
            throw new GameError.FullCapacityException();
        }
    }
    
    private static Player checkPlayerInGame(final Game game, final String playerId) {
        return game.getAllPlayers()
                .filter(p -> Objects.equals(p.getId(), playerId)).findFirst()
                .orElseThrow(() -> new PlayerError.NotFoundException(playerId));
    }
}
