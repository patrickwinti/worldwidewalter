package ch.zhaw.www.model;

import ch.zhaw.www.utils.InstantWrapper;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Model class with round information
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Round {
    @NotNull
    private final String id;
    @NotNull
    private final Prompt prompt;
    
    @Getter(AccessLevel.NONE)
    private final Duration propositionDuration;
    @Getter(AccessLevel.NONE)
    private final Duration enterLimitDuration;
    @Getter(AccessLevel.NONE)
    private final Duration selectionDuration;
    
    private final List<Proposition> propositions = new ArrayList<>();
    private final Map<String, String> selections = new ConcurrentHashMap<>();
    
    @Nullable
    private Player sphinx;
    @Nullable
    private Instant propositionSubmissionEnd;
    @Nullable
    private Instant selectionSubmissionEnd;
    
    /**
     * Sets the sphinx player for the round.
     *
     * @param sphinx The sphinx player
     */
    public void setSphinx(Player sphinx) {
        if (this.sphinx == null && sphinx != null) {
            this.sphinx = sphinx;
            this.propositionSubmissionEnd = InstantWrapper.offsetNow(propositionDuration);
            this.selectionSubmissionEnd = InstantWrapper.offsetNow(propositionDuration.plus(selectionDuration));
        }
    }
    
    /**
     * Adds a player's selection for a proposition.
     *
     * @param playerId      The ID of the player making the selection
     * @param propositionId The ID of the proposition being selected
     */
    public void addSelection(String playerId, String propositionId) {
        if (sphinx != null && !sphinx.getId().equals(playerId)) {
            selections.put(playerId, propositionId);
        }
    }
    
    /**
     * Checks whether the round can be entered by players.
     *
     * @return true if the round can be entered; false otherwise
     */
    public boolean canEnterRound() {
        return propositionSubmissionEnd != null && InstantWrapper.isAfterNow(propositionSubmissionEnd.minus(enterLimitDuration));
    }
    
    /**
     * Adds a player's proposition for the prompt.
     *
     * @param proposition The proposition to be added
     */
    public void addProposition(Proposition proposition) {
        propositions.add(proposition);
    }
    
    /**
     * Gets the state of the round.
     *
     * @return the state of the round
     */
    State getState() {
        if (sphinx == null) {
            return State.CREATED;
        } else if (canSendPropositions()) {
            return State.OPEN_FOR_SUBMISSIONS;
        } else if (canSendSelections()) {
            return State.OPEN_FOR_SELECTIONS;
        } else {
            return State.FINISHED;
        }
    }
    
    /*
     * Checks whether player can send a proposition
     */
    private boolean canSendPropositions() {
        return propositionSubmissionEnd != null && InstantWrapper.isAfterNow(propositionSubmissionEnd);
    }
    
    /*
     * Checks whether players can still make their selections.
     *
     * @return true if players can still make their selections; false otherwise
     */
    private boolean canSendSelections() {
        return selectionSubmissionEnd != null && InstantWrapper.isAfterNow(selectionSubmissionEnd);
    }
    
    /*
     * Gets the number of selections submitted by players.
     *
     * @return the number of selections submitted
     */
    int getNumberOfSelectionsSubmitted() {
        return selections.size();
    }
    
    /*
     * Gets the number of propositions submitted by players.
     *
     * @return the number of propositions submitted
     */
    int getNumberOfPropositionsSubmitted() {
        return propositions.size();
    }
    
    /**
     * Enum representing the possible states of a round.
     */
    enum State {
        CREATED, OPEN_FOR_SUBMISSIONS, OPEN_FOR_SELECTIONS, FINISHED
    }
}
