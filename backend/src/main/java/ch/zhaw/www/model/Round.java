package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final int roundPropositionInterval;
    @Getter(AccessLevel.NONE)
    private final Map<Player, String> propositions = new ConcurrentHashMap<>();
    @Getter(AccessLevel.NONE)
    private final Map<Player, String> selections = new ConcurrentHashMap<>();
    @NotNull
    private Player sphinx;
    private Instant propositionSubmissionEnd;
    
    boolean waitingForPropositions() {
        return selections.isEmpty();
    }
    
    public void startPropositionTimer() {
        propositionSubmissionEnd = Instant.now().plus(roundPropositionInterval, ChronoUnit.MINUTES);
    }
    
}
