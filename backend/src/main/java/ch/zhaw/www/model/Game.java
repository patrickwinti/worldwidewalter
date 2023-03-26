package ch.zhaw.www.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class with all the current state of a game.
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
@KeySpace("running_games")
public class Game {
    private static final int MINIMUM_AMOUNT_OF_PLAYERS = 4;
    @Id
    @NotNull
    private final String id;
    private final int numberOfRoundsInTurn = 1;
    
    private final List<Round> rounds = new ArrayList<>();
    private final Map<String, Player> waitingRoom = new HashMap<>();
    private final Map<String, Player> activePlayers = new HashMap<>();
    
    public boolean needsNewRound() {
        return getGameState() == State.WAITING_FOR_PLAYER;
    }
    
    public void addRound(Round round) {
        rounds.add(round);
    }
    
    /**
     * Gets last running round. A round counts as running
     * when the Sphinx has been selected
     *
     * @return {@link Round} or null
     */
    @Nullable
    public Round getRunningRound() {
        if (rounds.isEmpty()) {
            return null;
        } else {
            var round = rounds.get(rounds.size() - 1);
            return round.getSphinx() != null ? round : null;
        }
    }
    
    /**
     * Returns state of the current game:
     * - Waiting for player: Not enough players active or no valid round
     * - Waiting for propositions: Not all players send propositions
     * - Waiting for selections: Not all players send propositions
     *
     * @return {@link Game.State}
     */
    public State getGameState() {
        var round = getRunningRound();
        if (activePlayers.size() < MINIMUM_AMOUNT_OF_PLAYERS || round == null) {
            return State.WAITING_FOR_PLAYER;
        } else if (round.propositionsSent() < activePlayers.size()) {
            return State.WAITING_FOR_ALL_PROPOSITIONS;
        } else if (round.selectionsSent() < activePlayers.size()) {
            return State.WAITING_FOR_SELECTIONS;
        } else {
            return State.WAITING_FOR_PLAYER;
        }
    }
    
    public Prompt getNextPrompt() {
        return new Prompt("I've always wanted to WALTER", 1);
    }
    
    public enum State {
        WAITING_FOR_PLAYER, WAITING_FOR_ALL_PROPOSITIONS, WAITING_FOR_SELECTIONS
    }
}
