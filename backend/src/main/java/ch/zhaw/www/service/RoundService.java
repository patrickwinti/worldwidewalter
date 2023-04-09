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
     * Selects candidates depending on in which round the game is in
     * If game the game is in the first round of turn, it fetches it from candidates
     * Otherwise, it fetches it from the last round
     *
     * @param game in which the sphinx is to be selected
     */
    void selectSphinx(Game game);
    
    /**
     * Creates new round in game
     *
     * @param game game to have a new round
     */
    void createNewRound(Game game);
    
    /**
     * Returns round with given identifier
     *
     * @param roundId  round identifier
     * @param playerId player requesting round
     * @return new or existing round
     * @throws GameError.NotFoundException      if game is not found
     * @throws RoundError.IllegalStateException if there are not enough players anymore
     */
    Round getRound(@NotNull String roundId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException;
    
    /**
     * Player submitted their propositions for the prompt
     *
     * @param roundId  round identifier
     * @param playerId player identifier
     * @throws RoundError.NotFoundException  if round is not found
     * @throws PlayerError.NotFoundException if player is not found
     */
    void submitProposition(@NotNull String roundId, @NotNull String playerId, @NotNull List<String> gaps) throws RoundError.NotFoundException, PlayerError.NotFoundException;
    
    /**
     * Player has chosen a proposition id. The given player id is anonymized and only valid for the round
     *
     * @param roundId       round identifier
     * @param playerId      player identifier
     * @param propositionId proposition identifier
     * @throws RoundError.NotFoundException  if round is not found
     * @throws PlayerError.NotFoundException if player is not found
     */
    void selectProposition(@NotNull String roundId, @NotNull String playerId, @NotNull String propositionId) throws RoundError.NotFoundException, PlayerError.NotFoundException, PropositionError.NotFoundException;
}
