package ch.zhaw.www.model;

import ch.zhaw.www.service.RoundError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static ch.zhaw.www.TestHelper.createPlayer;
import static ch.zhaw.www.TestHelper.createProposition;
import static ch.zhaw.www.TimeHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class RoundTest {
    private static final String ID = "ROUND_ID";
    private static final Prompt PROMPT = new Prompt("WALTER", 1);
    private static final Duration PROPOSITION_DURATION = Duration.ofMinutes(40);
    private static final Duration PROPOSITION_ENTER_LIMIT = Duration.ofMinutes(10);
    private static final Duration SUBMISSION_DURATION = Duration.ofMinutes(10);
    
    @BeforeEach
    void setUp() {
        enableFixedClocked();
    }
    
    @AfterEach
    void tearDown() {
        disableFixedClocked();
    }
    
    @Test
    void whenStartingRoundForPropositionSubmissionTheConfiguredValueIsAddedToNow() {
        Round round = getRound();
        
        assertNull(round.getPropositionSubmissionEnd());
        
        round.setSphinx(createPlayer());
        
        assertNotNull(round.getPropositionSubmissionEnd());
        assertEquals(getFixedClockInstant().plus(PROPOSITION_DURATION), round.getPropositionSubmissionEnd());
    }
    
    @Test
    void roundCanNotBeEnteredIfAMinuteBeforePropositionSubmissionEnd() {
        Round round = getRound();
        round.setSphinx(createPlayer());
        
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        offsetFixedClockBy(PROPOSITION_DURATION.minus(PROPOSITION_ENTER_LIMIT).minus(1, ChronoUnit.MINUTES));
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        assertTrue(round.canEnterRound());
        offsetFixedClockBy(PROPOSITION_DURATION.minus(PROPOSITION_ENTER_LIMIT));
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        assertFalse(round.canEnterRound());
    }
    
    @Test
    void propositionsCanBeSubmittedIf_PropositionEndTimePassed() {
        Round round = getRound();
        round.setSphinx(createPlayer());
        
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        
        offsetFixedClockBy(PROPOSITION_DURATION.minus(1, ChronoUnit.MINUTES));
        
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        round.addProposition(createProposition("1", "Fish "));
        
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        offsetFixedClockBy(PROPOSITION_DURATION);
        assertEquals(Round.State.OPEN_FOR_SELECTIONS, round.getState());
    }
    
    @Test
    void selectionsCanBeSubmitted() {
        Round round = getRound();
        
        assertEquals(Round.State.CREATED, round.getState());
        
        round.setSphinx(createPlayer());
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        
        round.addProposition(createProposition("1", "Joseph"));
        
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        offsetFixedClockBy(PROPOSITION_DURATION);
        
        round.addProposition(createProposition("2", "Joseph"));
        
        assertEquals(Round.State.OPEN_FOR_SELECTIONS, round.getState());
    }
    
    @Test
    void addProposition() {
        Round round = getRound();
        Proposition proposition1 = new Proposition(UUID.randomUUID().toString(), "1", List.of("Bruce", "Martha", "Selina"));
        assertThrows(RoundError.IllegalStateException.class, () -> round.addProposition(proposition1));
        Proposition proposition2 = new Proposition(UUID.randomUUID().toString(), "2", List.of("Barry", "Wally"));
        round.setSphinx(createPlayer());
        round.addProposition(proposition2);
        assertEquals(1, round.getPropositions().size());
    }
    
    private static Round getRound() {
        return new Round(ID, PROMPT, PROPOSITION_DURATION, PROPOSITION_ENTER_LIMIT, SUBMISSION_DURATION);
    }
}