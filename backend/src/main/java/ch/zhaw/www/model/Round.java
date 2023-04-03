package ch.zhaw.www.model;

import ch.zhaw.www.service.RoundError;
import ch.zhaw.www.utils.InstantWrapper;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
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
    
    private final Map<String, Proposition> propositions = new ConcurrentHashMap<>();
    private final Map<String, String> selections = new ConcurrentHashMap<>();
    
    @Nullable
    private Player sphinx;
    @Nullable
    private Instant propositionSubmissionEnd;
    @Nullable
    private Instant selectionSubmissionEnd;
    
    public void setSphinx(Player sphinx) {
        if (this.sphinx == null) {
            this.sphinx = sphinx;
            this.propositionSubmissionEnd = InstantWrapper.offsetNow(propositionDuration);
            this.selectionSubmissionEnd = InstantWrapper.offsetNow(propositionDuration.plus(selectionDuration));
        }
    }
    
    /**
     * Adds player selection for proposition
     *
     * @param playerId      player selecting a proposition. cannot be the sphinx
     * @param propositionId proposition being selected
     */
    public void addSelection(String playerId, String propositionId) {
        if (sphinx != null && !sphinx.getId().equals(playerId)) {
            selections.put(playerId, propositionId);
        }
    }
    
    /**
     * Adds player proposition for prompt
     *
     * @param playerId    player adding their proposition
     * @param proposition as many propositions as gaps in the prompt
     */
    public void addProposition(String playerId, List<String> proposition) {
        if (sphinx == null) {
            throw new RoundError.IllegalStateException();
        }
        propositions.put(playerId, proposition);
    }
    
    public boolean canEnterRound() {
        return propositionSubmissionEnd != null && InstantWrapper.isAfterNow(propositionSubmissionEnd.minus(enterLimitDuration));
    }

    public void addProposition(String playerId, Proposition proposition) {
        getPropositions().put(playerId, proposition);
    }
    
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

    
    private boolean canSendPropositions() {
        return propositionSubmissionEnd != null && InstantWrapper.isAfterNow(propositionSubmissionEnd);
    }
    
    private boolean canSendSelections() {
        return selectionSubmissionEnd != null && InstantWrapper.isAfterNow(selectionSubmissionEnd);
    }
    
    int getNumberOfSelectionsSubmitted() {
        return selections.size();
    }
    
    int getNumberOfPropositionsSubmitted() {
        return propositions.size();
    }
    
    enum State {
        CREATED, OPEN_FOR_SUBMISSIONS, OPEN_FOR_SELECTIONS, FINISHED
    }
}
