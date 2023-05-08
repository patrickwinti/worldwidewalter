package ch.zhaw.www.service;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.model.Round;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
class RoundServiceImpl implements RoundService {
    private static final int ONE_ROUND_LEFT = 1;
    
    private final GameProperties gameProperties;
    private final EntityService entityService;
    private final EvaluationService evaluationService;
    
    /**
     * Constructs a RoundServiceImpl object with the specified GameProperties and EntityService instances.
     *
     * @param gameProperties the GameProperties instance to use
     * @param entityService  the EntityService instance to use
     */
    RoundServiceImpl(GameProperties gameProperties,
                     EntityService entityService,
                     EvaluationService evaluationService) {
        this.gameProperties = gameProperties;
        this.entityService = entityService;
        this.evaluationService = evaluationService;
    }
    
    @Override
    public void selectSphinx(Game game) {
        if (!game.hasEnoughPlayers()) return;
        
        var sphinxCandidates = game.getSphinxCandidates();
        if (sphinxCandidates.isEmpty()) return;
        
        var round = game.getCurrentRoundOptional();
        if (round.isEmpty() || round.get().getSphinx() != null) return;
        
        var temp = new HashMap<>(sphinxCandidates);
        Player sphinx = null;
        do {
            var player = Collections.min(temp.entrySet(), Map.Entry.comparingByValue());
            temp.remove(player.getKey());
            if (game.hasActivePlayer(player.getKey().getId())) {
                if (player.getValue() > ONE_ROUND_LEFT) {
                    sphinxCandidates.put(player.getKey(), player.getValue() - 1);
                } else {
                    sphinxCandidates.remove(player.getKey());
                }
                sphinx = player.getKey();
            }
        } while (!temp.isEmpty() && sphinx == null);
        game.setSphinxCandidates(sphinxCandidates);
        round.get().setSphinx(sphinx);
    }
    
    @Override
    public Round createNewRound(final Game game) {
        return new Round(UUID.randomUUID().toString(),
                game.consumePrompt(),
                gameProperties.getPropositionSubmissionDuration(),
                gameProperties.getRoundEnterLimitDuration(),
                gameProperties.getSelectionSubmissionDuration());
    }
    
    @Override
    public void submitProposition(@NotNull String roundId, @NotNull String playerId, @NotNull List<String> gaps) {
        verifyPlayerIsActive(roundId, playerId);
        entityService.editRound(roundId, (game, round) -> {
            if (round.getSphinx() == null) {
                throw new RoundError.IllegalStateException();
            }
            round.getPropositions()
                    .stream()
                    .filter(proposition -> proposition.hasSameGaps(gaps))
                    .findFirst()
                    .ifPresentOrElse(proposition -> proposition.submittedBy(playerId), () -> {
                        Proposition proposition = new Proposition(UUID.randomUUID().toString(), gaps);
                        proposition.submittedBy(playerId);
                        round.addProposition(proposition);
                    });
            return round;
        });
    }
    
    @Override
    public void selectProposition(@NotNull String roundId, @NotNull String playerId, @NotNull String propositionId) {
        verifyPlayerIsActive(roundId, playerId);
        entityService.editRound(roundId, (game, round) -> {
            if (!round.isSphinx(playerId) && round.hasProposition(propositionId)) {
                round.addSelection(playerId, propositionId);
                var evaluation = evaluationService.evaluateSelection(round, propositionId, playerId);
                game.addPoints(evaluation);
                return game;
            } else {
                throw new RoundError.IllegalOperationException();
            }
        });
    }
    
    @Override
    public Round getRoundReadyForSelections(final @NotNull String roundId, final @NotNull String playerId) throws RoundError.NotFoundException, RoundError.IllegalStateException, PlayerError.NotFoundException {
        verifyPlayerIsActive(roundId, playerId);
        var gameRoundPair = entityService.getGameForRound(roundId);
        if (!gameRoundPair.getFirst().canAcceptSelectionForRound(gameRoundPair.getSecond())) {
            throw new RoundError.IllegalStateException();
        }
        return gameRoundPair.getSecond();
    }
    
    @Override
    public Round getRoundClosedForSelections(@NotNull String roundId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException {
        Pair<Game, Round> gameRoundPair = entityService.getGameForRound(roundId);
        verifyPlayerInGameOfRound(gameRoundPair.getSecond().getId(), playerId);
        if (!gameRoundPair.getFirst().isRoundFinished(gameRoundPair.getSecond())) {
            throw new RoundError.IllegalStateException();
        }
        return gameRoundPair.getSecond();
    }
    
    @Override
    public Game getGameForRound(String roundId) throws GameError.NotFoundException {
        return entityService.getGameForRound(roundId).getFirst();
    }
    
    /**
     * Verifies that the player with the specified ID is active in the round with the specified ID.
     *
     * @param roundId  the ID of the round to verify the player for
     * @param playerId the ID of the player to verify
     */
    private void verifyPlayerIsActive(String roundId, String playerId) {
        if (!entityService.isPlayerActiveInRound(roundId, playerId)) {
            throw new PlayerError.NotFoundException(playerId);
        }
    }
    
    /**
     * Verifies that the player with the specified ID is registered in the game with the specified ID.
     *
     * @param roundId  the ID of the round to verify the player for
     * @param playerId the ID of the player to verify
     */
    private void verifyPlayerInGameOfRound(String roundId, String playerId) {
        if (!entityService.isPlayerRegisteredInGameOfRound(roundId, playerId)) {
            throw new PlayerError.NotFoundException(playerId);
        }
    }
    
}
