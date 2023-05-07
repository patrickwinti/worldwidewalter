package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.repository.GameRepository;
import ch.zhaw.www.utils.GameTransaction;
import ch.zhaw.www.utils.InstantWrapper;
import ch.zhaw.www.utils.RoundTransaction;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;
import java.util.stream.StreamSupport;

@Service
class EntityServiceImpl implements EntityService {
    
    private final GameRepository gamesRepository;
    
    /**
     * Constructor that injects the GameRepository.
     *
     * @param gamesRepository The GameRepository to be injected
     */
    EntityServiceImpl(GameRepository gamesRepository) {
        this.gamesRepository = gamesRepository;
    }
    
    @Override
    public Game getGame(@NotNull String gameId) throws GameError.NotFoundException {
        synchronized (gamesRepository) {
            return findGame(gameId);
        }
    }
    
    @Override
    public <T> T editGame(@NotNull String gameId, GameTransaction<T> editor) throws GameError.NotFoundException {
        synchronized (gamesRepository) {
            var game = findGame(gameId);
            T result = editor.transactionalChange(game);
            game.setLastEdit(InstantWrapper.getNow());
            gamesRepository.save(game);
            return result;
        }
    }
    
    @Override
    public Pair<Game, Round> getGameForRound(final String roundId) {
        synchronized (gamesRepository) {
            return findGameForRound(roundId);
        }
    }
    
    @Override
    public Round getRound(@NotNull String roundId) throws RoundError.NotFoundException {
        synchronized (gamesRepository) {
            return findGameForRound(roundId).getSecond();
        }
    }
    
    @Override
    public <T> T editRound(@NotNull String roundId, RoundTransaction<T> editor) throws RoundError.NotFoundException {
        synchronized (gamesRepository) {
            var gameRoundPair = findGameForRound(roundId);
            T result = editor.transactionalChange(gameRoundPair.getFirst(), gameRoundPair.getSecond());
            gameRoundPair.getFirst().setLastEdit(InstantWrapper.getNow());
            gamesRepository.save(gameRoundPair.getFirst());
            return result;
        }
    }
    
    @Override
    public boolean isPlayerActiveInRound(final @NotNull String roundId, final @NotNull String playerId) throws RoundError.NotFoundException {
        return findGameForRound(roundId).getFirst().hasActivePlayer(playerId);
    }
    
    @Override
    public void saveNewGame(@NotNull Game game) {
        synchronized (gamesRepository) {
            if (!gamesRepository.existsById(game.getId())) {
                game.setLastEdit(InstantWrapper.getNow());
                gamesRepository.save(game);
            } else {
                throw new GameError.ExistAlready();
            }
        }
    }
    
    /**
     * Finds a game in the repository by ID.
     *
     * @param gameId The ID of the game to find
     * @return The game with the specified ID
     * @throws GameError.NotFoundException if no game with the specified ID is found
     */
    
    private Game findGame(String gameId) {
        return gamesRepository.findById(gameId).orElseThrow(() -> new GameError.NotFoundException(gameId));
    }
    
    /*
     * Finds the game that contains a round with the specified ID.
     *
     * @param roundId The ID of the round to find
     * @return The game that contains the round with the specified ID
     * @throws RoundError.NotFoundException if no round with the specified ID is found
     */
    private Pair<Game, Round> findGameForRound(String roundId) {
        Predicate<Round> matchesRound = round -> round.getId().equals(roundId);
        return StreamSupport.stream(gamesRepository.findAll().spliterator(), true)
                .filter(game -> game.getRoundHistory().anyMatch(matchesRound))
                .findFirst()
                .map(game -> Pair.of(game, game.getRoundHistory()
                        .filter(matchesRound)
                        .findFirst()
                        .orElseThrow(() -> new RoundError.NotFoundException(roundId))))
                .orElseThrow(() -> new RoundError.NotFoundException(roundId));
    }
}
