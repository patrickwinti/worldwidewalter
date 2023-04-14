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

@Service
class GameServiceImpl implements GameService {
    private static final Logger LOGGER = Logger.getLogger(GameService.class.getSimpleName());
    private static final int DEFAULT_NUMBER_OF_ROUNDS = 1;
    
    private final EntityService entityService;
    private final GameProperties gameProperties;
    private final RoundService roundService;
    private final PostfixGenerator postfixGenerator;
    private final GameIdGenerator gameIdGenerator;

    GameServiceImpl(EntityService entityService, GameProperties gameProperties, RoundService roundService,
                    final PostfixGenerator postfixGenerator, GameIdGenerator gameIdGenerator) {
        this.entityService = entityService;
        this.gameProperties = gameProperties;
        this.roundService = roundService;
        this.postfixGenerator = postfixGenerator;
        this.gameIdGenerator = gameIdGenerator;
    }
    
    @Override
    public Game createGame() throws GameError.ExistAlready {
        var game = new Game(gameIdGenerator.generateId(),
                gameProperties.getMinimumAmountOfActivePlayersPerGame(),
                gameProperties.getMaximumAmountOfActivePlayersPerGame(),
                DEFAULT_NUMBER_OF_ROUNDS);
        entityService.saveNewGame(game);
        return game;
    }
    
    @Override
    public Game getGame(String gameId) throws GameError.NotFoundException {
        return entityService.getGame(gameId);
    }
    
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
    
    @Override
    public void leaveGame(String gameId, String playerId) throws GameError.NotFoundException {
        entityService.editGame(gameId, game -> {
            if (!game.hasPlayer(playerId)) {
                throw new PlayerError.NotFoundException(playerId);
            }
            game.removePlayer(playerId);
        });
    }
    
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
    
    @Override
    public Round getCurrentRoundInGame(String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException {
        Game game = entityService.getGame(gameId);
        if (game.getState() != Game.State.WAITING_FOR_ALL_PROPOSITIONS || !game.hasActivePlayer(playerId)) {
            throw new RoundError.IllegalStateException();
        }
        return game.getCurrentRound();
    }
    
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
