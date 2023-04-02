package ch.zhaw.www.service;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.model.Round;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.StreamSupport;

@Service
class GameServiceImpl implements GameService {
    private final GameEntityService gameEntityService;
    private final GameProperties gameProperties;
    private final PostfixGenerator postfixGenerator = new PostfixGenerator();

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
        String uuid = UUID.randomUUID().toString();
        gameEntityService.editGame(gameId, game -> {
            StringBuilder name = new StringBuilder(playerName);
            while (game.getAllPlayers().anyMatch(player -> name.toString().equals(player.getName()))) {
                name.append(postfixGenerator.getRandomPostfix());
            }
            Player tempPlayer = new Player(uuid, name.toString());
            game.getWaitingRoom().put(tempPlayer.getId(), tempPlayer);
            return game;
        });
        return gameEntityService.getGame(gameId).getWaitingRoom().get(uuid);
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
        gameEntityService.editGameForRound(roundId, game -> {
            for (Proposition proposition : game.getCurrentRound().getPropositions().values()) {
                if (checkForDuplicates(proposition.getGaps(), gaps)) {
                    proposition.getDuplicates().add(temp);
                    return game;
                }
            }
            game.getCurrentRound().addProposition(playerId, temp);
            return game;
        });

    }

    @Override
    public void selectProposition(String roundId, String playerId, String propositionId) throws GameError.NotFoundException,
            RoundError.NotFoundException, PlayerError.NotFoundException, PropositionError.NotFoundException {

    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    private boolean checkForDuplicates(List<String> existingPropositionGaps, List<String> newPropositionGaps) {
        return existingPropositionGaps.size() == newPropositionGaps.size() &&
                String.join("", existingPropositionGaps).equalsIgnoreCase(String.join("", newPropositionGaps));
    }

}
