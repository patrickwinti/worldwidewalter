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

import static ch.zhaw.www.utils.InstantWrapper.isInFuture;

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
    @Setter
    private int tempSphinxPoints = 0;
    @Setter
    private boolean hasNonSphinxPropositionBeenSelected = false;
    
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
        selections.put(playerId, propositionId);
    }
    
    /**
     * Checks whether the round can be entered by players.
     *
     * @return true if the round can be entered; false otherwise
     */
    public boolean canEnterRound() {
        return propositionSubmissionEnd == null || isInFuture(propositionSubmissionEnd.minus(enterLimitDuration));
    }
    
    /**
     * Adds a player's proposition for the prompt.
     *
     * @param proposition The proposition to be added
     */
    public void addProposition(Proposition proposition) {
        propositions.add(proposition);
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
        return propositions.stream()
                .map(proposition -> proposition.getPlayerIds().size())
                .reduce(0, Integer::sum);
    }
    
    /**
     * Checks whether player can send a proposition
     *
     * @return true if player can still submit propositions
     */
    boolean acceptsPropositions() {
        return sphinx != null && propositionSubmissionEnd != null && isInFuture(propositionSubmissionEnd);
    }
    
    /*
     * Checks whether players can still make their selections.
     *
     * @return true if players can still make their selections; false otherwise
     */
    boolean acceptsSelections() {
        return !acceptsPropositions() && selectionSubmissionEnd != null && isInFuture(selectionSubmissionEnd);
    }
    
    /**
     * Checks if round has timed out and no proposition or selections are possible
     *
     * @return true if round is finished
     */
    boolean isFinished() {
        return sphinx != null && !acceptsSelections() && !acceptsPropositions();
    }
    
    /**
     * Checks if player with given playerId is sphinx in this round
     *
     * @param playerId the playerId to be checked
     * @return boolean if playerId is sphinx
     */
    public boolean isSphinx(String playerId) {
        return sphinx != null && sphinx.getId().equals(playerId);
    }
    
    /**
     * Checks if proposition with given propositionId exists in round
     *
     * @param propositionId the propositionId to be checked
     * @return boolean if proposition exists in round
     */
    public boolean hasProposition(String propositionId) {
        return propositions.stream().anyMatch(proposition -> proposition.getId().equals(propositionId));
    }
}
