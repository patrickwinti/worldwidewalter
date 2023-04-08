package ch.zhaw.www.service;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.*;
import ch.zhaw.www.utils.GameIdGenerator;
import ch.zhaw.www.utils.PostfixGenerator;
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
    private final PostfixGenerator postfixGenerator = new PostfixGenerator();
    
    GameServiceImpl(GameEntityService gameEntityService, GameProperties gameProperties) {
        this.gameEntityService = gameEntityService;
        this.gameProperties = gameProperties;
    }
    
    @Override
    public Game createGame() throws GameError.ExistAlready {
        var game = new Game(GameIdGenerator.generateId(),
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
    public String enterGame(String gameId, String playerName) throws GameError.NotFoundException, GameError.FullCapacityException {
        String uuid = UUID.randomUUID().toString();
        gameEntityService.editGame(gameId, game -> {
            StringBuilder name = new StringBuilder(playerName);
            while (game.getAllPlayers().anyMatch(player -> name.toString().equals(player.getName()))) {
                name.append(postfixGenerator.getRandomPostfix());
            }
            Player tempPlayer = new Player(uuid, name.toString());
            game.addPlayerToWaitingRoom(tempPlayer);
            return game;
        });
        return uuid;
    }
    
    @Override
    public void leaveGame(String gameId, String playerId) throws GameError.NotFoundException {
        gameEntityService.editGame(gameId, game -> {
            game.removePlayer(playerId);
            return game;
        });
    }
    
    @Override
    public void enterRound(String gameId, String playerId) throws GameError.NotFoundException, PlayerError.NotFoundException {
        gameEntityService.editGame(gameId, game -> {
            Player player = game.getAllPlayers()
                    .filter(p -> Objects.equals(p.getId(), playerId)).findFirst()
                    .orElseThrow(() -> new PlayerError.NotFoundException(playerId));
            Game.State state = game.getState();
            switch (state) {
                case NO_VALID_ROUND -> {
                    createNewRound(game, game.consumePrompt());
                    LOGGER.log(Level.INFO, "Creating a new round for game {0}", game);
                    game.moveToActivePlayers(player);
                }
                case WAITING_FOR_PLAYERS -> {
                    game.moveToActivePlayers(player);
                    LOGGER.log(Level.INFO, "Adding player to round {0}", gameId);
                }
                case WAITING_FOR_ALL_PROPOSITIONS -> {
                    game.moveToActivePlayers(player);
                    LOGGER.log(Level.INFO, "Adding player to round {0}", gameId);
                    chooseSphinx(game);
                }
                case WAITING_FOR_ALL_SELECTIONS -> {
                    //Player can't enter round at the moment. Player will stay in waiting room.
                }
            }
            LOGGER.log(Level.INFO, () -> String.format("Game %s moved to state: %s", gameId, game.getState()));
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
    public void submitProposition(String roundId, String playerId, List<String> gaps) throws GameError.NotFoundException,
            RoundError.NotFoundException, PlayerError.NotFoundException {
        gameEntityService.editGameForRound(roundId, game -> {
            if (!game.hasActivePlayer(playerId)) {
                throw new PlayerError.NotFoundException(playerId);
            }
            Proposition temp = new Proposition(UUID.randomUUID().toString(), playerId, gaps);
            final Round round = Objects.requireNonNull(game.getCurrentRound());
            for (Proposition proposition : round.getPropositions()) {
                if (proposition.hasSameGaps(temp)) {
                    proposition.getDuplicates().add(temp);
                    return game;
                }
            }
            round.addProposition(temp);
            return game;
        });
        
    }
    
    @Override
    public void selectProposition(String roundId, String playerId, String propositionId) throws GameError.NotFoundException,
            RoundError.NotFoundException, PlayerError.NotFoundException, PropositionError.NotFoundException {
        
    }
    
    @Override
    public Round getRound(String roundId, String playerId) throws RoundError.NotFoundException, PlayerError.NotFoundException {
        var game = gameEntityService.getGameForRound(roundId);
        if (game.hasPlayer(playerId)) {
            return game.getCurrentRound();
        } else {
            throw new PlayerError.NotFoundException(playerId);
        }
    }
    
    private void createNewRound(final Game game, Prompt prompt) {
        game.addRound(new Round(UUID.randomUUID().toString(),
                prompt,
                gameProperties.getPropositionSubmissionDuration(),
                gameProperties.getRoundEnterLimitDuration(),
                gameProperties.getSelectionSubmissionDuration()));
    }
    
    private void chooseSphinx(final Game game) {
        var round = Objects.requireNonNull(game.getCurrentRound());
        if (round.getSphinx() == null) {
            round.setSphinx(game.selectSphinx());
        }
    }
}
