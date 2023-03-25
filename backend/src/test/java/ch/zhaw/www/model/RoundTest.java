package ch.zhaw.www.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class RoundTest {
    private static final String ID = "ROUND_ID";
    private static final Prompt PROMPT = new Prompt("WALTER");
    
    @Test
    void whenStartingRoundForPropositionSubmissionTheConfiguredValueIsAddedToNow() {
        var round = new Round(ID, PROMPT, 2000);
        assertNull(round.getPropositionSubmissionEnd());
        round.startPropositionTimer();
        assertNotNull(round.getPropositionSubmissionEnd());
        assertTrue(round.getPropositionSubmissionEnd().isAfter(Instant.now().plus(1500, ChronoUnit.MINUTES)));
    }
}