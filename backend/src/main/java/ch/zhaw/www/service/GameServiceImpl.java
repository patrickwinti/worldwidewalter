package ch.zhaw.www.service;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.repository.GameRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;
    private final GameProperties gameProperties;
    
    GameServiceImpl(GameRepository gameRepository, GameProperties gameProperties) {
        this.gameRepository = gameRepository;
        this.gameProperties = gameProperties;
    }
    
    @Override
    public Game createGame() {
        var game = new Game(UUID.randomUUID().toString());
        gameRepository.saveNewGame(game);
        return game;
    }
    
    @Override
    public Game getGame(String gameId) throws GameError.NotFoundException {
        return gameRepository.getGame(gameId);
    }
    
    @Override
    public Player enterGame(String gameId, String playerName) throws GameError.NotFoundException, GameError.FullCapacityException {
        gameRepository.editGame(gameId, this::startNewRound);
        return new Player(playerName);
    }
    
    @Override
    public void leaveGame(String gameId, String playerId) throws GameError.NotFoundException {
    
    }
    
    @Override
    public Round getRound(String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException {
        Game game = gameRepository.getGame(gameId);
        if (game.getGameState() != Game.State.WAITING_FOR_ALL_PROPOSITIONS || !game.getActivePlayers().containsKey(playerId)) {
            throw new RoundError.IllegalStateException();
        }
        return game.getRunningRound();
    }
    
    @Override
    public void submitProposition(String roundId, String playerId, List<String> gaps) throws GameError.NotFoundException,
            RoundError.NotFoundException, PlayerError.NotFoundException {
        
    }
    
    @Override
    public void selectProposition(String roundId, String playerId, String propositionId) throws GameError.NotFoundException,
            RoundError.NotFoundException, PlayerError.NotFoundException, PropositionError.NotFoundException {
    }
    
    private @NotNull Game startNewRound(@NotNull Game game) {
        if (game.getGameState() == Game.State.WAITING_FOR_PLAYERS) {
            game.addRound(new Round(generateId(),
                    game.getNextPrompt(),
                    gameProperties.getPropositionSubmissionDuration(),
                    gameProperties.getRoundEnterLimit()));
        }
        return game;
    }
    
    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
