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
    
    private final List<Prompt> prompts = List.of(new Prompt("I've always wanted to WALTER", 1));
    
    public void addRound(Round round) {
        waitingRoom.putAll(activePlayers);
        activePlayers.clear();
        rounds.add(round);
    }
    
    /**
     * Gets current round,
     * when the Sphinx has been selected
     *
     * @return {@link Round} or null
     */
    @Nullable
    public Round getCurrentRound() {
        if (rounds.isEmpty()) {
            return null;
        } else {
            var round = rounds.get(rounds.size() - 1);
            return round.getState() != Round.State.FINISHED ? round : null;
        }
    }
    
    /**
     * Returns state of the current game:
     * - Waiting for player: Not enough players active or no valid round
     * - Waiting for propositions: Not all players did send propositions and is in time for sending any
     * - Waiting for selections: Not all players did send propositions
     *
     * @return {@link Game.State}
     */
    public State getState() {
        var round = getCurrentRound();
        var numberOfActivePlayers = activePlayers.size();
        if (round == null) {
            return State.NO_VALID_ROUND;
        } else if (numberOfActivePlayers < MINIMUM_AMOUNT_OF_PLAYERS) {
            return State.WAITING_FOR_PLAYERS;
        } else if (round.getState() == Round.State.OPEN_FOR_SUBMISSIONS &&
                round.getNumberOfPropositionsSubmitted() < numberOfActivePlayers) {
            return State.WAITING_FOR_ALL_PROPOSITIONS;
        } else if (round.getState() == Round.State.OPEN_FOR_SELECTIONS &&
                round.getNumberOfSelectionsSubmitted() < numberOfActivePlayers) {
            return State.WAITING_FOR_ALL_SELECTIONS;
        } else {
            return State.NO_VALID_ROUND;
        }
    }
    
    public Prompt consumePrompt() {
        //return prompts.remove(0) ;
        return prompts.get(0);
    }
    
    public enum State {
        NO_VALID_ROUND, WAITING_FOR_PLAYERS, WAITING_FOR_ALL_PROPOSITIONS, WAITING_FOR_ALL_SELECTIONS
    }
}
