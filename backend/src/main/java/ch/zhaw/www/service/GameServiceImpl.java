package ch.zhaw.www.service;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
class GameServiceImpl implements GameService {
    private static final Logger LOGGER = Logger.getLogger(GameService.class.getSimpleName());
    private static final int DEFAULT_NUMBER_OF_ROUNDS = 1;
    
    private final GameEntityService gameEntityService;
    private final GameProperties gameProperties;
    
    GameServiceImpl(GameEntityService gameEntityService, GameProperties gameProperties) {
        this.gameEntityService = gameEntityService;
        this.gameProperties = gameProperties;
    }
    
    @Override
    public Game createGame() {
        var game = new Game(UUID.randomUUID().toString(),
                gameProperties.getMinimumAmountOfActivePlayersPerGame(),
                gameProperties.getMaximumAmountOfActivePlayersPerGame(),
                DEFAULT_NUMBER_OF_ROUNDS);
        gameEntityService.saveNewGame(game);
        return game;
    }
    
    @Override
    public Game getGame(String gameId) throws GameError.NotFoundException {
        return gameEntityService.getGame(gameId);
    }
    
    @Override
    public Player enterGame(String gameId, String playerName) throws GameError.NotFoundException, GameError.FullCapacityException {
        var player = new Player(UUID.randomUUID().toString(), playerName);
        getGame(gameId).addPlayerToWaitingRoom(player);
        return player;
    }
    
    @Override
    public void leaveGame(String gameId, String playerId) throws GameError.NotFoundException {
    
    }
    
    @Override
    public void enterRound(String gameId, @Valid String playerId) throws GameError.NotFoundException, PlayerError.NotFoundException {
        gameEntityService.editGame(gameId, game -> {
            Player player = game.getAllPlayers()
                    .filter(p -> Objects.equals(p.getId(), playerId)).findFirst()
                    .orElseThrow(() -> new PlayerError.NotFoundException(playerId));
            Game.State state = game.getState();
            switch (state) {
                case NO_VALID_ROUND -> {
                    game.addRound(new Round(UUID.randomUUID().toString(),
                            game.consumePrompt(),
                            gameProperties.getPropositionSubmissionDuration(),
                            gameProperties.getRoundEnterLimitDuration(),
                            gameProperties.getSelectionSubmissionDuration()));
                    LOGGER.log(Level.INFO, "Creating a new round for game {0}", gameId);
                    game.markPlayerAsActive(player);
                }
                case WAITING_FOR_PLAYERS, WAITING_FOR_ALL_PROPOSITIONS -> {
                    game.markPlayerAsActive(player);
                    LOGGER.log(Level.INFO, "Adding player to round {0}", gameId);
                }
                case WAITING_FOR_ALL_SELECTIONS -> {
                    //Player can't enter round at the moment. Player will stay in waiting room.
                }
            }
            return game;
        });
    }
    
    @Override
    public Round getCurrentRoundInGame(String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException {
        Game game = gameEntityService.getGame(gameId);
        if (game.getState() != Game.State.WAITING_FOR_ALL_PROPOSITIONS || !game.hasActivePlayer(playerId)) {
            throw new RoundError.IllegalStateException();
        }
        return game.getCurrentRound();
    }
    
    @Override
    public void submitProposition(String roundId, String playerId, List<String> gaps) throws RoundError.NotFoundException, PlayerError.NotFoundException {
        
    }
    
    @Override
    public void selectProposition(String roundId, String playerId, String propositionId) throws RoundError.NotFoundException, PlayerError.NotFoundException, PropositionError.NotFoundException {
    }
    
    @Override
    public Round getRound(String roundId, String playerId) throws RoundError.NotFoundException, PlayerError.NotFoundException {
        var game = gameEntityService.getGameForRound(roundId);
        if (game.getAllPlayers().anyMatch(player -> player.getId().equals(playerId))) {
            return game.getCurrentRound();
        } else {
            throw new PlayerError.NotFoundException(playerId);
        }
    }
}
