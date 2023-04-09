package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;
import java.util.stream.StreamSupport;

@Service
class EntityServiceImpl implements EntityService {
    
    private final GameRepository gamesRepository;
    
    EntityServiceImpl(GameRepository gamesRepository) {
        this.gamesRepository = gamesRepository;
    }
    
    @Override
    public Game getGame(String gameId) throws GameError.NotFoundException {
        synchronized (gamesRepository) {
            return findGame(gameId);
        }
    }
    
    @Override
    public void editGame(String gameId, Consumer<Game> editor) throws GameError.NotFoundException {
        synchronized (gamesRepository) {
            var game = findGame(gameId);
            gamesRepository.save(editor.apply(game));
        }
    }
    
    @Override
    public Round getRound(String roundId) throws RoundError.NotFoundException {
        synchronized (gamesRepository) {
            return findGameForRound(roundId).getCurrentRound();
        }
    }
    
    @Override
    public void editRound(String roundId, Consumer<Round> editor) throws RoundError.NotFoundException {
        synchronized (gamesRepository) {
            var game = findGameForRound(roundId);
            editor.accept(game.getCurrentRound());
            gamesRepository.save(game);
        }
    }
    
    @Override
    public boolean isPlayerActiveInRound(final String roundId) throws RoundError.NotFoundException {
        return false;
    }
    
    @Override
    public void saveNewGame(Game game) {
        synchronized (gamesRepository) {
            if (!gamesRepository.existsById(game.getId())) {
                gamesRepository.save(game);
            } else {
                throw new GameError.ExistAlready();
            }
        }
    }
    
    private Game findGame(String gameId) {
        return gamesRepository.findById(gameId).orElseThrow(() -> new GameError.NotFoundException(gameId));
    }
    
    private Game findGameForRound(String roundId) {
        return StreamSupport.stream(gamesRepository.findAll().spliterator(), true)
                .filter(game -> {
                    var round = game.getCurrentRound();
                    return round != null && round.getId().equals(roundId);
                })
                .findFirst().orElseThrow(() -> new RoundError.NotFoundException(roundId));
    }
    
}
