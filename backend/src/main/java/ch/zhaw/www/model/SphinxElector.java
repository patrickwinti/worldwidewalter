package ch.zhaw.www.model;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Handles the Sphinx management. Saves candidates and selects the next possible.
 */
@AllArgsConstructor
class SphinxElector {
    private static final int MINIMUM_ROUNDS_POSSIBLE = 1;
    private final List<Round> rounds;
    private final Map<String, Player> activePlayers;
    
    private final List<Player> candidates = new ArrayList<>();
    private final Random random = new Random();
    
    /**
     * Selects candidates depending on in which round the game is in
     * If game the game is in the first round of turn, it fetches it from candidates
     * Otherwise, it fetches it from the last round
     *
     * @param numberOfRoundsInTurn number of rounds per turn
     * @return a sphinx if it could be found or null
     */
    @Nullable
    public Player selectCandidate(final int numberOfRoundsInTurn) {
        if (activePlayers.isEmpty() || numberOfRoundsInTurn < MINIMUM_ROUNDS_POSSIBLE) {
            return null;
        }
        if (candidates.isEmpty()) {
            candidates.addAll(activePlayers.values());
        }
        if (numberOfRoundsInTurn == 1 || rounds.size() % numberOfRoundsInTurn == 1) {
            //new turn
            var playerIndex = random.nextInt(candidates.size());
            return candidates.remove(playerIndex);
        } else {
            //find sphinx from last turn
            return getSphinxFromLastRound();
        }
    }
    
    @Nullable
    private Player getSphinxFromLastRound() {
        for (int i = rounds.size() - 1; i >= 0; i--) {
            var lastRound = rounds.get(i);
            if (lastRound.getSphinx() != null) {
                return lastRound.getSphinx();
            }
        }
        return null;
    }
    
    public void addCandidate(final Player player) {
        candidates.add(player);
    }
}
