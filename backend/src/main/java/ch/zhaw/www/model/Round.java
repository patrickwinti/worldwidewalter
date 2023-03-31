package ch.zhaw.www.model;

import ch.zhaw.www.utils.InstantWrapper;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Model class with round information
 */
@Data
public class Round {
    @NotNull
    private final String id;
    @NotNull
    private final Prompt prompt;
    
    @Getter(AccessLevel.NONE)
    private final Duration propositionDuration;
    @Getter(AccessLevel.NONE)
    private final Duration enterLimitDuration;
    
    private final Map<String, List<String>> propositions = new ConcurrentHashMap<>();
    private final Map<String, String> selections = new ConcurrentHashMap<>();
    
    @Nullable
    private Player sphinx;
    @Setter(AccessLevel.NONE)
    @Nullable
    private Instant propositionSubmissionEnd;
    @Setter(AccessLevel.NONE)
    @Nullable
    private Instant selectionSubmissionEnd;
    
    public void setSphinx(Player sphinx) {
        this.sphinx = sphinx;
        this.propositionSubmissionEnd = InstantWrapper.offsetNow(propositionDuration);
        //TODO: Change for more reasonable time
        this.selectionSubmissionEnd = InstantWrapper.offsetNow(propositionDuration.plus(propositionDuration));
    }
    
    State getState() {
        if (sphinx == null) {
            return State.CREATED;
        } else if (canEnterRound()) {
            return State.OPEN_FOR_SUBMISSIONS;
        } else if (!propositions.isEmpty()) {
            return State.OPEN_FOR_SELECTIONS;
        } else {
            return State.FINISHED;
        }
    }
    
    private boolean canEnterRound() {
        return propositionSubmissionEnd != null && InstantWrapper.isAfterNow(propositionSubmissionEnd.minus(enterLimitDuration));
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
