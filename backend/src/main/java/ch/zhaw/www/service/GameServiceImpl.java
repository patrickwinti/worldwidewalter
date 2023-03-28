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
import java.util.stream.Collectors;

@Service
class GameServiceImpl implements GameService {
    private final GameEntityService gameEntityService;
    private final GameProperties gameProperties;
    
    GameServiceImpl(GameEntityService gameEntityService, GameProperties gameProperties) {
        this.gameEntityService = gameEntityService;
        this.gameProperties = gameProperties;
    }
    
    @Override
    public Game createGame() {
        var game = new Game(UUID.randomUUID().toString());
        gameEntityService.saveNewGame(game);
        return game;
    }
    
    @Override
    public Game getGame(String gameId) throws GameError.NotFoundException {
        return gameEntityService.getGame(gameId);
    }
    
    @Override
    public Player enterGame(String gameId, String playerName) throws GameError.NotFoundException, GameError.FullCapacityException {
        return new Player(playerName);
    }
    
    @Override
    public void leaveGame(String gameId, String playerId) throws GameError.NotFoundException {
    
    }
    
    @Override
    public Round enterRound(String gameId, @Valid String playerId) throws GameError.NotFoundException, PlayerError.NotFoundException {
        gameEntityService.editGame(gameId, game -> {
            if (game.getState() == Game.State.NO_VALID_ROUND) {
                game.addRound(new Round(generateId(),
                        game.consumePrompt(),
                        gameProperties.getPropositionSubmissionDuration().getSeconds(),
                        gameProperties.getRoundEnterLimitDuration().getSeconds()));
            }
            Player player = game.getAllPlayers()
                    .filter(p -> Objects.equals(p.getId(), playerId)).findFirst()
                    .orElseThrow(() -> new PlayerError.NotFoundException(playerId));
            game.markPlayerAsActive(player);
            return game;
        });
        return gameEntityService.getGame(gameId).getCurrentRound();
    }
    
    @Override
    public Round getRound(String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException {
        Game game = gameEntityService.getGame(gameId);
        if (game.getState() != Game.State.WAITING_FOR_ALL_PROPOSITIONS || !game.getActivePlayers().containsKey(playerId)) {
            throw new RoundError.IllegalStateException();
        }
        return game.getCurrentRound();
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
    
    private String generateId() {
        return UUID.randomUUID().toString();
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
