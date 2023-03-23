package ch.zhaw.www.repository;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.service.GameError;
import org.springframework.stereotype.Repository;

import java.util.function.UnaryOperator;

@Repository
public class GameRepositoryImpl implements GameRepository {
    private final RunningGamesRepository gamesRepository;
    
    public GameRepositoryImpl(RunningGamesRepository gamesRepository) {
        this.gamesRepository = gamesRepository;
    }
    
    @Override
    public Game getGame(String gameId) throws GameError.NotFoundException {
        return gamesRepository.findById(gameId).orElseThrow(() -> new GameError.NotFoundException(gameId));
    }
    
    @Override
    public void editGame(String gameId, UnaryOperator<Game> editor) throws GameError.NotFoundException {
        var game = getGame(gameId);
        synchronized (game) {
            gamesRepository.save(editor.apply(game));
        }
    }
    
    @Override
    public void saveNewGame(Game game) {
        if (gamesRepository.findById(game.getId()).isEmpty()) {
            gamesRepository.save(game);
        } else {
            throw new RuntimeException("Game already exists");
        }
    }
}
