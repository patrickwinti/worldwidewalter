package ch.zhaw.www.model;

import ch.zhaw.www.utils.InstantWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class RoundTest {
    private static final String ID = "ROUND_ID";
    private static final Prompt PROMPT = new Prompt("WALTER", 1);
    private static final int PROPOSITION_DURATION = 40;
    private static final int PROPOSITION_ENTER_LIMIT = 10;
    private final Instant instant = Instant.parse("2022-12-22T10:15:30Z");
    private final Clock fixedClock = Clock.fixed(instant, ZoneId.of("UTC"));
    
    private static Round getRound() {
        return new Round(ID, PROMPT, PROPOSITION_DURATION, PROPOSITION_ENTER_LIMIT);
    }
    
    @BeforeEach
    void setUp() {
        InstantWrapper.clock = fixedClock;
    }
    
    @Test
    void whenStartingRoundForPropositionSubmissionTheConfiguredValueIsAddedToNow() {
        Round round = getRound();
        
        assertNull(round.getPropositionSubmissionEnd());
        
        round.openForPropositionSubmission();
        
        assertNotNull(round.getPropositionSubmissionEnd());
        assertEquals(instant.plus(PROPOSITION_DURATION, ChronoUnit.MINUTES), round.getPropositionSubmissionEnd());
    }
    
    @Test
    void roundCanNotBeEnteredIfAMinuteBeforePropositionSubmissionEnd() {
        Round round = getRound();
        round.openForPropositionSubmission();
        
        assertTrue(round.canEnterRound());
        tick(fixedClock, PROPOSITION_DURATION - PROPOSITION_ENTER_LIMIT - 1);
        assertTrue(round.canEnterRound());
        tick(fixedClock, PROPOSITION_DURATION - PROPOSITION_ENTER_LIMIT);
        assertFalse(round.canEnterRound());
    }
    
    @Test
    void propositionsCanBeSubmittedIfSphinxIsSet() {
        Round round = getRound();
        round.openForPropositionSubmission();
        
        assertFalse(round.canPropositionsBeSubmitted());
        
        round.setSphinx(new Player(UUID.randomUUID().toString()));
        assertTrue(round.canPropositionsBeSubmitted());
    }
    
    @Test
    void propositionsCanBeSubmittedIf_PropositionEndTimePassed() {
        Round round = getRound();
        round.setSphinx(new Player(UUID.randomUUID().toString()));
        
        assertFalse(round.canPropositionsBeSubmitted());
        
        round.openForPropositionSubmission();
        assertTrue(round.canPropositionsBeSubmitted());
        
        tick(fixedClock, PROPOSITION_DURATION - 1);
        
        assertTrue(round.canPropositionsBeSubmitted());
        
        tick(fixedClock, PROPOSITION_DURATION);
        
        assertFalse(round.canPropositionsBeSubmitted());
    }
    
    private void tick(Clock clock, int minutes) {
        InstantWrapper.clock = Clock.offset(clock, Duration.of(minutes, ChronoUnit.MINUTES));
    }
    
}