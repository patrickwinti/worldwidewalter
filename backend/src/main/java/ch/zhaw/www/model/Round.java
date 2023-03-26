package ch.zhaw.www.model;

import ch.zhaw.www.utils.InstantWrapper;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

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
    @Getter(AccessLevel.NONE)
    private final Map<String, String> propositions = new ConcurrentHashMap<>();
    @Getter(AccessLevel.NONE)
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
