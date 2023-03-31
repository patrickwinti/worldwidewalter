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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class RoundTest {
    private static final String ID = "ROUND_ID";
    private static final Prompt PROMPT = new Prompt("WALTER", 1);
    private static final int PROPOSITION_DURATION = 40;
    private static final int PROPOSITION_ENTER_LIMIT = 10;
    private final Instant instant = Instant.parse("2022-12-22T10:00:00Z");
    private final Clock fixedClock = Clock.fixed(instant, ZoneId.of("UTC"));
    
    private static Round getRound() {
        return new Round(ID, PROMPT, PROPOSITION_DURATION, PROPOSITION_ENTER_LIMIT);
    }
    
    @BeforeEach
    void setUp() {
        InstantWrapper.clock = fixedClock;
    }
    
    private static Player getSphinx() {
        return new Player(UUID.randomUUID().toString());
    }
    
    @Test
    void whenStartingRoundForPropositionSubmissionTheConfiguredValueIsAddedToNow() {
        Round round = getRound();
        
        assertNull(round.getPropositionSubmissionEnd());
        
        round.setSphinx(getSphinx());
        
        assertNotNull(round.getPropositionSubmissionEnd());
        assertEquals(instant.plus(PROPOSITION_DURATION, ChronoUnit.MINUTES), round.getPropositionSubmissionEnd());
    }
    
    @Test
    void roundCanNotBeEnteredIfAMinuteBeforePropositionSubmissionEnd() {
        Round round = getRound();
        round.setSphinx(getSphinx());
        
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        tick(fixedClock, PROPOSITION_DURATION - PROPOSITION_ENTER_LIMIT - 1);
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        tick(fixedClock, PROPOSITION_DURATION - PROPOSITION_ENTER_LIMIT);
        assertEquals(Round.State.FINISHED, round.getState());
    }
    
    @Test
    void propositionsCanBeSubmittedIf_PropositionEndTimePassed() {
        Round round = getRound();
        round.setSphinx(getSphinx());
        
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        
        tick(fixedClock, PROPOSITION_DURATION - 1);
        
        assertEquals(Round.State.FINISHED, round.getState());
        round.getPropositions().put("1", new Proposition(UUID.randomUUID().toString(), List.of("Fish ")));
        
        assertEquals(Round.State.OPEN_FOR_SELECTIONS, round.getState());
    }
    
    private void tick(Clock clock, int minutes) {
        InstantWrapper.clock = Clock.offset(clock, Duration.of(minutes, ChronoUnit.MINUTES));
    }
    
    @Test
    void selectionsCanBeSubmitted() {
        Round round = getRound();
        Proposition proposition1 = new Proposition("1", List.of("Joseph"));
        Proposition proposition2 = new Proposition("1", List.of("Joseph"));

        assertEquals(Round.State.CREATED, round.getState());
        
        round.setSphinx(getSphinx());
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        
        round.getPropositions().put("1", proposition1);
        
        assertEquals(Round.State.OPEN_FOR_SUBMISSIONS, round.getState());
        tick(fixedClock, PROPOSITION_DURATION);
        
        round.getPropositions().put("2", proposition2);
        
        assertEquals(Round.State.OPEN_FOR_SELECTIONS, round.getState());
    }

    @Test
    void addProposition() {
        Round round = getRound();
        Proposition proposition1 = new Proposition("1", List.of("Bruce", "Martha", "Selina"));
        Proposition proposition2 = new Proposition("2", List.of("Barry", "Wally"));

    }

    
}