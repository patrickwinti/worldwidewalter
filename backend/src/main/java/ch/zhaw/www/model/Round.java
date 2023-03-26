package ch.zhaw.www.model;

import ch.zhaw.www.utils.InstantWrapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
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
    private final int propositionDuration;
    private final int enterLimit;
    private final Map<String, String> propositions = new ConcurrentHashMap<>();
    private final Map<String, String> selections = new ConcurrentHashMap<>();
    @NotNull
    private Player sphinx;
    private Instant propositionSubmissionEnd;
    
    int propositionsSent() {
        return propositions.size();
    }
    
    int selectionsSent() {
        return selections.size();
    }
    
    boolean canSelectionsBeSubmitted() {
        return sphinx != null && !propositions.isEmpty();
    }
    
    boolean canPropositionsBeSubmitted() {
        return sphinx != null && InstantWrapper.isAfterNow(propositionSubmissionEnd, 0);
    }
    
    boolean canEnterRound() {
        return InstantWrapper.isAfterNow(propositionSubmissionEnd, -enterLimit);
    }
    
    public void openForPropositionSubmission() {
        propositionSubmissionEnd = InstantWrapper.offsetNowMinutes(propositionDuration);
    }
    
}
