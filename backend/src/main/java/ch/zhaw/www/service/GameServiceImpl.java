package ch.zhaw.www.service;

import ch.zhaw.www.model.*;
import ch.zhaw.www.repository.GameRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public Round getRound(String gameId, @NotNull String playerId) throws GameError.NotFoundException, GameError.NotEnoughPlayersException {
        return new Round(UUID.randomUUID().toString(), new Prompt("I've always wanted to WALTER", 1));
    }
    
    @Override
    public void submitProposition(String roundId, String playerId, List<String> gaps) throws GameError.NotFoundException,
            RoundError.NotFoundException, PlayerError.NotFoundException {

        Proposition temp = new Proposition(UUID.randomUUID().toString(), gaps);

        //todo check if correct, most likely need to retrieve round out of a round list
        for (Proposition proposition : getRound(roundId).getPropositions().values()) {
            if (checkForDuplicates(proposition.getGaps(), gaps)) {
                proposition.getDuplicates().add(temp);
                return;
            }
        }
        // todo check if correct, most likely would need to retrieve player out of a player list
        getRound(roundId).getPropositions().put(new Player(playerId), temp);

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

    private boolean checkForDuplicates(List<String> existingProposition, List<String> newProposition) {
        return existingProposition.size() == newProposition.size() &&
                existingProposition.stream()
                        .map(String::toLowerCase)
                        .toList()
                        .equals(newProposition.stream()
                                .map(String::toLowerCase)
                                .collect(Collectors.toList()));
    }

}
