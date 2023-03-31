package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.function.UnaryOperator;
import java.util.stream.StreamSupport;

@Service
class GameEntityServiceImpl implements GameEntityService {
    
    private final GameRepository gamesRepository;
    
    GameEntityServiceImpl(GameRepository gamesRepository) {
        this.gamesRepository = gamesRepository;
    }
    
    @Override
    public Game getGame(String gameId) throws GameError.NotFoundException {
        synchronized (gamesRepository) {
            return gamesRepository.findById(gameId).orElseThrow(() -> new GameError.NotFoundException(gameId));
        }
    }
    
    @Override
    public void editGame(String gameId, UnaryOperator<Game> editor) throws GameError.NotFoundException {
        synchronized (gamesRepository) {
            var game = getGame(gameId);
            gamesRepository.save(editor.apply(game));
        }
    }
    
    @Override
    public void saveNewGame(Game game) {
        synchronized (gamesRepository) {
            if (gamesRepository.findById(game.getId()).isEmpty()) {
                gamesRepository.save(game);
            } else {
                throw new RuntimeException("Game already exists");
            }
        }
    }
    @Override
    public void editGameForRound(String roundId, UnaryOperator<Game> editor) throws RoundError.NotFoundException {
        synchronized (gamesRepository) {
            var game = StreamSupport.stream(gamesRepository.findAll().spliterator(), true)
                    .filter(g -> {
                        var round = g.getCurrentRound();
                        return round != null && round.getId().equals(roundId);
                    })
                    .findFirst().orElseThrow(() -> new RoundError.NotFoundException(roundId));
            gamesRepository.save(editor.apply(game));
        }
    }
}
