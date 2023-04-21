package ch.zhaw.www.service;

import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.model.Round;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static ch.zhaw.www.TestHelper.createProposition;
import static ch.zhaw.www.TestHelper.createRound;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EvaluationServiceIntegrationTest {
    
    private final EvaluationService evaluationService = new EvaluationServiceImpl();
    private static final String sphinxId = "Sphinx-ID";
    private final Proposition player1Proposition = createProposition("Player-ID-1", "orange");
    private final Proposition player2Proposition = createProposition("Player-ID-2", "green");
    private final Proposition player3Proposition = createProposition("Player-ID-3", "red");
    private final Proposition sphinxProposition = createProposition(sphinxId, "blue");
    private Round round;
    
    @BeforeEach
    void setUp() {
        round = createRound();
        round.addProposition(player1Proposition);
        round.addProposition(player2Proposition);
        round.addProposition(player3Proposition);
        round.addProposition(sphinxProposition);
        round.setSphinx(new Player(sphinxId, "Sphinx-Name"));
    }
    
    @Test
    void evaluateNonSphinxSelectedWithoutTempPoints() {
        String idOfSelectedProposition = player1Proposition.getId();
        String propositionOriginatorId = player1Proposition.getPlayerId();
        String selectorId = player2Proposition.getPlayerId();
        
        Map<String, Integer> result = evaluationService.evaluateSelection(round, idOfSelectedProposition, selectorId);
        
        assertEquals(2, result.size());
        assertEquals(1, result.get(propositionOriginatorId));
        assertEquals(0, result.get(sphinxId));
    }
    
    @Test
    void evaluateNonSphinxSelectedWithoutWithTempPoints() {
        String idOfSelectedProposition = sphinxProposition.getId();
        
        evaluationService.evaluateSelection(round, idOfSelectedProposition, player2Proposition.getPlayerId());
        assertEquals(1, round.getTempSphinxPoints());
        assertFalse(round.isAtLeastOneNoneSphinxPropositionHasBeenSelected());
        
        evaluationService.evaluateSelection(round, idOfSelectedProposition, player3Proposition.getPlayerId());
        assertEquals(2, round.getTempSphinxPoints());
        assertFalse(round.isAtLeastOneNoneSphinxPropositionHasBeenSelected());
        
        Map<String, Integer> result = evaluationService.evaluateSelection(round, player1Proposition.getId(), player3Proposition.getPlayerId());
        assertEquals(0, round.getTempSphinxPoints());
        assertTrue(round.isAtLeastOneNoneSphinxPropositionHasBeenSelected());
        
        assertEquals(2, result.size());
        assertEquals(1, result.get(player1Proposition.getPlayerId()));
        assertEquals(2, result.get(sphinxId));
    }
    
}
