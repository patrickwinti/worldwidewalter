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
import java.util.Random;
import java.util.UUID;

import static java.util.UUID.randomUUID;

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
        var game = new Game(randomUUID().toString());
        gameEntityService.saveNewGame(game);
        return game;
    }
    
    @Override
    public Game getGame(String gameId) throws GameError.NotFoundException {
        return gameEntityService.getGame(gameId);
    }
    
    @Override
    public Player enterGame(String gameId, String playerName) throws GameError.NotFoundException, GameError.FullCapacityException {
        return new Player(playerName,randomUUID().toString());
    }
    
    @Override
    public void leaveGame(String gameId, String playerId) throws GameError.NotFoundException {
        if (getGame(gameId).getWaitingRoom().containsKey(playerId)) {
            getGame(gameId).getWaitingRoom().remove(playerId);
        }
        if (getGame(gameId).getActivePlayers().containsKey(playerId)) {
            getGame(gameId).getActivePlayers().remove(playerId);
        }
        if (getGame(gameId).getSphinxCandidates().contains(playerId)) {
            getGame(gameId).getSphinxCandidates().remove(playerId);
        }
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
        
    }
    
    @Override
    public void selectProposition(String roundId, String playerId, String propositionId) throws GameError.NotFoundException,
            RoundError.NotFoundException, PlayerError.NotFoundException, PropositionError.NotFoundException {
    }
    
    private String generateId() {
        return randomUUID().toString();
    }

    public void selectSphinx(String gameId, Round round) throws GameError.NotFoundException {
        if (getGame(gameId).getSphinxCandidates().isEmpty()) {
            getGame(gameId).getSphinxCandidates().addAll(getGame(gameId).getActivePlayers().values());
        }
        int index = new Random().nextInt(getGame(gameId).getSphinxCandidates().size());
        Player newSphinx = getGame(gameId).getSphinxCandidates().get(index);
        getGame(gameId).getSphinxCandidates().remove(index);
    }











}



