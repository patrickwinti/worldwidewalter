package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Round;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Handles the Sphinx management. Based on the current state of the game
 * in which round in the turn the game is in, the next Sphinx will be selected
 */
public interface RoundService {
    
    /**
     * Selects candidates depending on in which round the game is in.
     * Sphinx can be repeated if the turn has multiple rounds.
     * Players leaving the game will not be considered for Sphinx selection
     *
     * @param game in which the sphinx is to be selected
     */
    void selectSphinx(Game game);
    
    /**
     * Creates new round in game
     *
     * @param game game to have a new round
     * @return a new round
     */
    Round createNewRound(Game game);
    
    /**
     * Returns round with given identifier
     *
     * @param roundId  round identifier
     * @param playerId player requesting round
     * @return new or existing round
     * @throws RoundError.NotFoundException     if round is not found
     * @throws RoundError.IllegalStateException if not all players submitted their proposition
     * @throws PlayerError.NotFoundException    player not active in round
     */
    Round getRoundWithAllPropositions(@NotNull String roundId, @NotNull String playerId) throws RoundError.NotFoundException, RoundError.IllegalStateException, PlayerError.NotFoundException;
    
    /**
     * Player submits their propositions for the prompt
     *
     * @param roundId  round identifier
     * @param playerId player identifier
     * @throws RoundError.NotFoundException  if round is not found
     * @throws PlayerError.NotFoundException if player is not found
     */
    void submitProposition(@NotNull String roundId, @NotNull String playerId, @NotNull List<String> gaps) throws RoundError.NotFoundException, PlayerError.NotFoundException;
    
    /**
     * Player selects a proposition in given round.
     *
     * @param roundId       round identifier
     * @param playerId      player identifier
     * @param propositionId proposition identifier
     * @throws RoundError.NotFoundException  if round is not found
     * @throws PlayerError.NotFoundException if player is not found
     */
    void selectProposition(@NotNull String roundId, @NotNull String playerId, @NotNull String propositionId) throws RoundError.NotFoundException, PlayerError.NotFoundException, PropositionError.NotFoundException;
    
}
