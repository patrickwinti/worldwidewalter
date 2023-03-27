package ch.zhaw.www.model;

import ch.zhaw.www.utils.InstantWrapper;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
    
    private final long propositionDuration;
    private final long enterLimit;
    
    private final Map<String, List<String>> propositions = new ConcurrentHashMap<>();
    private final Map<String, String> selections = new ConcurrentHashMap<>();
    
    @Nullable
    private Player sphinx;
    @Nullable
    private Instant propositionSubmissionEnd;
    
    public void setSphinx(Player sphinx) {
        this.sphinx = sphinx;
        this.propositionSubmissionEnd = InstantWrapper.offsetNowMinutes(propositionDuration);
    }
    
    State getState() {
        if (sphinx == null) {
            return State.CREATED;
        } else if (InstantWrapper.isAfterNow(propositionSubmissionEnd, -enterLimit)) {
            return State.OPEN_FOR_SUBMISSIONS;
        } else if (!propositions.isEmpty()) {
            return State.OPEN_FOR_SELECTIONS;
        } else {
            return State.FINISHED;
        }
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
