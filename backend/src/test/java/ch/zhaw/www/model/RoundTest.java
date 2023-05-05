package ch.zhaw.www.model;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
class RoundTest {
    private static final String ID = "ROUND_ID";
    private static final Prompt PROMPT = new Prompt("<<walter>>", List.of("WALTER"));
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
        Proposition proposition1 = new Proposition(UUID.randomUUID().toString(), List.of("Bruce", "Martha", "Selina"));
        round.addProposition(proposition1);
        Proposition proposition2 = new Proposition(UUID.randomUUID().toString(), List.of("Barry", "Wally"));
        round.setSphinx(createPlayer());
        round.addProposition(proposition2);
        assertEquals(2, round.getPropositions().size());
    }
    
    @Test
    void addSelection() {
        Round round = getRound();
        String playerId = "playerId";
        String propositionId = "propositionId";
        round.addSelection(playerId, propositionId);
        
        assertTrue(round.getSelections().containsKey(playerId));
        assertEquals(propositionId, round.getSelections().get(playerId));
    }
    
    @Test
    void getNumberOfPropositionsSubmitted() {
        Proposition proposition1 = mock(Proposition.class);
        when(proposition1.getPlayerIds()).thenReturn(List.of("1", "2"));
        Proposition proposition2 = mock(Proposition.class);
        when(proposition2.getPlayerIds()).thenReturn(List.of("34"));
        
        Round round = getRound();
        round.addProposition(proposition1);
        round.addProposition(proposition2);
        
        assertEquals(3, round.getNumberOfPropositionsSubmitted());
    }
    
    @Test
    void hasProposition() {
        Proposition proposition1 = mock(Proposition.class);
        when(proposition1.getId()).thenReturn("propId1");
        Proposition proposition2 = mock(Proposition.class);
        when(proposition2.getId()).thenReturn("propId2");
        
        Round round = getRound();
        round.addProposition(proposition1);
        round.addProposition(proposition2);
        
        assertTrue(round.hasProposition("propId1"));
        assertTrue(round.hasProposition("propId2"));
        assertFalse(round.hasProposition("notExistingId"));
    }
    
    private static Round getRound() {
        return new Round(ID, PROMPT, PROPOSITION_DURATION, PROPOSITION_ENTER_LIMIT, SUBMISSION_DURATION);
    }
}