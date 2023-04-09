package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import jakarta.annotation.Nullable;

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
     * @return a sphinx if it could be found or null
     */
    @Nullable
    Player selectSphinx(Game game);
    
}
