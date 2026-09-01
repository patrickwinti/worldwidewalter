package ch.zhaw.www.service;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.model.Round;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
class RoundServiceImpl implements RoundService {
    private static final int ONE_ROUND_LEFT = 1;
    
    private final GameProperties gameProperties;
    private final EntityService entityService;
    private final EvaluationService evaluationService;
    private final GameNotifier gameNotifier;
    
    /**
     * Constructs a RoundServiceImpl object with the specified GameProperties and EntityService instances.
     *
     * @param gameProperties the GameProperties instance to use
     * @param entityService  the EntityService instance to use
     */
    RoundServiceImpl(GameProperties gameProperties,
                     EntityService entityService,
                     EvaluationService evaluationService,
                     GameNotifier gameNotifier) {
        this.gameProperties = gameProperties;
        this.entityService = entityService;
        this.evaluationService = evaluationService;
        this.gameNotifier = gameNotifier;
    }
    
    @Override
    public void selectSphinx(Game game) {
        if (!game.hasEnoughPlayers()) return;
        // Do not start the round until everyone who played the previous round has re-entered.
        if (!game.allExpectedPlayersEntered()) return;

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
                gameProperties.getSelectionSubmissionDuration(),
                gameProperties.getContinueWaitDuration());
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
            if (game.hasAllPropositions(round)) {
                // Start the selection phase now instead of leaving its deadline anchored at the
                // round start, which would add the unused proposition time to the selection time.
                round.closePropositionPhase();
            }
            return round;
        });
        notifyRoundChanged(roundId);
    }
    
    @Override
    public void selectProposition(@NotNull String roundId, @NotNull String playerId, @NotNull String propositionId) {
        verifyPlayerIsActive(roundId, playerId);
        verifyPropositionExists(roundId, propositionId);
        entityService.editRound(roundId, (game, round) -> {
            if (!round.isSphinx(playerId)) {
                round.addSelection(playerId, propositionId);
                var evaluation = evaluationService.evaluateSelection(round, propositionId, playerId);
                evaluation.entrySet().removeIf(entry -> !game.hasActivePlayer(entry.getKey()));
                game.addPoints(evaluation);
                round.addPoints(evaluation);
                return game;
            } else {
                throw new RoundError.IllegalOperationException();
            }
        });
        notifyRoundChanged(roundId);
    }
    
    @Override
    public Round getRoundReadyForSelections(final @NotNull String roundId, final @NotNull String playerId) throws RoundError.NotFoundException, RoundError.IllegalStateException, PlayerError.NotFoundException {
        verifyPlayerIsActive(roundId, playerId);
        var game = entityService.getGameForRound(roundId);
        var round = entityService.getRound(roundId);
        if (!game.canAcceptSelectionForRound(round)) {
            throw new RoundError.IllegalStateException();
        }
        return round;
    }
    
    @Override
    public Round getRoundClosedForSelections(@NotNull String roundId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException {
        var game = entityService.getGameForRound(roundId);
        var round = entityService.getRound(roundId);
        verifyPlayerInGameOfRound(roundId, playerId);
        if (!game.isRoundFinished(round)) {
            throw new RoundError.IllegalStateException();
        }
        return round;
    }
    
    @Override
    public Game getGameForRound(String roundId) throws GameError.NotFoundException {
        return entityService.getGameForRound(roundId);
    }
    
    /**
     * Pushes the current round status to the game's subscribers so clients can show what the
     * round is waiting for instead of polling blindly.
     *
     * @param roundId the round whose game should be notified
     */
    private void notifyRoundChanged(String roundId) {
        gameNotifier.notifyRoundChanged(entityService.getGameForRound(roundId));
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
    
    private void verifyPropositionExists(String roundId, String propositionId) {
        if (!entityService.getRound(roundId).hasProposition(propositionId)) {
            throw new PropositionError.NotFoundException(propositionId);
        }
    }
    
}
