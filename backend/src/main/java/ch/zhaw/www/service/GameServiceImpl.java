package ch.zhaw.www.service;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.bean.PostfixGenerator;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.utils.GameIdGenerator;
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
    private final PostfixGenerator postfixGenerator;
    
    /**
     * Constructor that injects the EntityService, GameProperties, RoundService, and PostfixGenerator.
     *
     * @param entityService The EntityService to be injected
     * @param gameProperties The GameProperties to be injected
     * @param roundService The RoundService to be injected
     * @param postfixGenerator The PostfixGenerator to be injected
     */
    GameServiceImpl(EntityService entityService, GameProperties gameProperties, RoundService roundService, final PostfixGenerator postfixGenerator) {
        this.entityService = entityService;
        this.gameProperties = gameProperties;
        this.roundService = roundService;
        this.postfixGenerator = postfixGenerator;
    }
    
    /**
     * Creates a new game and saves it to the repository.
     *
     * @return The newly created game
     * @throws GameError.ExistAlready if a game with the same ID already exists in the repository
     */
    @Override
    public Game createGame() throws GameError.ExistAlready {
        var game = new Game(GameIdGenerator.generateId(),
                gameProperties.getMinimumAmountOfActivePlayersPerGame(),
                gameProperties.getMaximumAmountOfActivePlayersPerGame(),
                DEFAULT_NUMBER_OF_ROUNDS);
        entityService.saveNewGame(game);
        return game;
    }
    
    /**
     * Gets a game from the repository by ID.
     *
     * @param gameId The ID of the game to retrieve
     * @return The game with the specified ID
     * @throws GameError.NotFoundException if no game with the specified ID is found
     */
    @Override
    public Game getGame(String gameId) throws GameError.NotFoundException {
        return entityService.getGame(gameId);
    }
    
    /**
     * Adds a player to the waiting room of a game.
     *
     * @param gameId The ID of the game to add the player to
     * @param playerName The name of the player to add
     * @return The ID of the player that was added
     * @throws GameError.NotFoundException if no game with the specified ID is found
     * @throws GameError.FullCapacityException if the game's waiting room is already full
     */
    @Override
    public String enterGame(String gameId, String playerName) throws GameError.NotFoundException, GameError.FullCapacityException {
        String uuid = UUID.randomUUID().toString();
        entityService.editGame(gameId, game -> {
            StringBuilder name = new StringBuilder(playerName);
            while (game.getAllPlayers().anyMatch(player -> name.toString().equals(player.getName()))) {
                name.append(postfixGenerator.getRandomPostfix());
            }
            Player tempPlayer = new Player(uuid, name.toString());
            game.addPlayerToWaitingRoom(tempPlayer);
        });
        return uuid;
    }
    /**
     * Removes a player from a game.
     *
     * @param gameId   the ID of the game to remove the player from
     * @param playerId the ID of the player to remove
     * @throws GameError.NotFoundException if the game cannot be found
     */
    @Override
    public void leaveGame(String gameId, String playerId) throws GameError.NotFoundException {
        entityService.editGame(gameId, game -> {
            if (!game.hasPlayer(playerId)) {
                throw new PlayerError.NotFoundException(playerId);
            }
            game.removePlayer(playerId);
        });
    }
    /**
     * Adds a player to a round in a game.
     *
     * @param gameId   the ID of the game to add the player to
     * @param playerId the ID of the player to add
     * @throws GameError.NotFoundException    if the game cannot be found
     * @throws PlayerError.NotFoundException  if the player cannot be found
     */
    @Override
    public void enterRound(String gameId, String playerId) throws GameError.NotFoundException, PlayerError.NotFoundException {
        entityService.editGame(gameId, game -> {
            Player player = game.getAllPlayers()
                    .filter(p -> Objects.equals(p.getId(), playerId)).findFirst()
                    .orElseThrow(() -> new PlayerError.NotFoundException(playerId));
            Game.State state = game.getState();
            switch (state) {
                case NO_VALID_ROUND -> {
                    roundService.createNewRound(game);
                    LOGGER.log(Level.INFO, "Creating a new round for game {0}", game);
                    movePlayerToActive(game, player);
                }
                case WAITING_FOR_PLAYERS, WAITING_FOR_ALL_PROPOSITIONS -> {
                    movePlayerToActive(game, player);
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
    /**
     * Returns the current round in a game for a specified player.
     *
     * @param gameId   the ID of the game to retrieve the round from
     * @param playerId the ID of the player to retrieve the round for
     * @return the current round in the game
     * @throws GameError.NotFoundException     if the game cannot be found
     * @throws RoundError.IllegalStateException if the player is not active or the game is not in the correct state
     */
    @Override
    public Round getCurrentRoundInGame(String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException {
        Game game = entityService.getGame(gameId);
        if (game.getState() != Game.State.WAITING_FOR_ALL_PROPOSITIONS || !game.hasActivePlayer(playerId)) {
            throw new RoundError.IllegalStateException();
        }
        return game.getCurrentRound();
    }
    /**
     * Moves a player from the waiting room to the list of active players in a game, if possible.
     *
     * @param game   the game to move the player to
     * @param player the player to move to the active list
     * @throws GameError.FullCapacityException    if there is no more space for active players
     * @throws PlayerError.NotFoundException      if the player cannot be found
     */
    private static void movePlayerToActive(final Game game, final Player player) {
        if (game.hasCapacityForNewActivePlayer()) {
            if (!game.hasPlayer(player.getId())) {
                throw new PlayerError.NotFoundException(player.getId());
            }
            game.moveToActivePlayers(player);
        } else {
            throw new GameError.FullCapacityException();
        }
    }
    
}
