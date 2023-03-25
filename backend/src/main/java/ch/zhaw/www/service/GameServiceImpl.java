package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Prompt;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.repository.GameRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;

    GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public Game createGame() {
        var game = new Game(UUID.randomUUID().toString());
        saveGame(game);
        return game;
    }

    @Override
    public Game getGame(String gameId) throws GameError.NotFoundException {
        return findGame(gameId);
    }

    @Override
    public Player enterGame(String gameId, String playerName) throws GameError.NotFoundException, GameError.FullCapacityException {
        return new Player(playerName);
    }

    @Override
    public void leaveGame(String gameId, String playerId) throws GameError.NotFoundException {

    }

    @Override
    public Round getRound(String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.OngoingException,
            GameError.NotEnoughPlayersException {
        return new Round(UUID.randomUUID().toString(), new Prompt("I've always wanted to WALTER", 1));
    }

    @Override
    public void submitProposition(String roundId, String playerId, List<String> gaps) throws GameError.NotFoundException,
            RoundError.NotFoundException, PlayerError.NotFoundException {

    }

    @Override
    public void selectProposition(String roundId, String playerId, String propositionId) throws GameError.NotFoundException,
            RoundError.NotFoundException, PlayerError.NotFoundException, PropositionError.NotFoundException {
    }

    private Game findGame(String gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> new GameError.NotFoundException(gameId));
    }

    private void saveGame(Game game) {
        gameRepository.save(game);
    }
}
