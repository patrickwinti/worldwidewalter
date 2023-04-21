package ch.zhaw.www.service;

import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static ch.zhaw.www.TestHelper.createProposition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class EvaluationServiceTest {
    
    private final EvaluationService evaluationService = new EvaluationServiceImpl();
    private static final String sphinxId = "Sphinx-ID";
    
    private Round mockRoundInRepository(int tempSphinxPoints, boolean atLeastOneNoneSphinxPropositionHasBeenSelected) {
        Round roundMock = mock(Round.class);
        when(roundMock.getPropositions()).thenReturn(List.of(
                createProposition("Player-ID-1", "orange"),
                createProposition("Player-ID-2", "green"),
                createProposition("Player-ID-3", "red"),
                createProposition(sphinxId, "blue")
        ));
        
        when(roundMock.getSphinx()).thenReturn(new Player(sphinxId, "Sphinx-Name"));
        when(roundMock.isAtLeastOneNoneSphinxPropositionHasBeenSelected()).thenReturn(atLeastOneNoneSphinxPropositionHasBeenSelected);
        when(roundMock.getTempSphinxPoints()).thenReturn(tempSphinxPoints);
        
        return roundMock;
    }
    
    @Test
    void evaluateSelectionOfNonSphinxPropositionWithTempSpinxPoints() {
        
        Round roundMock = mockRoundInRepository(3, false);
        String idOfSelectedProposition = roundMock.getPropositions().get(0).getId();
        String propositionOriginatorId = roundMock.getPropositions().get(0).getPlayerId();
        String selectorId = roundMock.getPropositions().get(2).getPlayerId();
        
        Map<String, Integer> evaluation = evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, selectorId);
        List<Integer> distributedPoints = List.copyOf(evaluation.values());
        List<String> playerIds = List.copyOf(evaluation.keySet());
        verify(roundMock).setTempSphinxPoints(0);
        verify(roundMock).setAtLeastOneNoneSphinxPropositionHasBeenSelected(true);
        
        assertEquals(2, evaluation.size());
        
        assertEquals(propositionOriginatorId, playerIds.get(0));
        assertEquals(1, distributedPoints.get(0));
        
        assertEquals(sphinxId, playerIds.get(1));
        assertEquals(3, distributedPoints.get(1));
        
    }
    
    @Test
    void evaluateSelectionOfOnlySphinxPropositions() {
        
        Round roundMock = mockRoundInRepository(3, false);
        String idOfSelectedProposition = roundMock.getPropositions().get(3).getId();
        String selectorId = roundMock.getPropositions().get(2).getPlayerId();
        
        Map<String, Integer> evaluation = evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, selectorId);
        verify(roundMock).setTempSphinxPoints(4);
        List<Integer> distributedPoints = List.copyOf(evaluation.values());
        List<String> playerIds = List.copyOf(evaluation.keySet());
        
        assertEquals(1, evaluation.size());
        
        assertEquals(selectorId, playerIds.get(0));
        assertEquals(1, distributedPoints.get(0));
    }
    
    @Test
    void evaluateSelectionOfSphinxPropositionWithNoTempSpinxPoints() {
        
        Round roundMock = mockRoundInRepository(0, true);
        String idOfSelectedProposition = roundMock.getPropositions().get(3).getId();
        String selectorId = roundMock.getPropositions().get(2).getPlayerId();
        
        Map<String, Integer> evaluation = evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, selectorId);
        List<Integer> distributedPoints = List.copyOf(evaluation.values());
        List<String> playerIds = List.copyOf(evaluation.keySet());
        
        assertEquals(2, evaluation.size());
        
        assertEquals(selectorId, playerIds.get(0));
        assertEquals(1, distributedPoints.get(0));
        
        assertEquals(sphinxId, playerIds.get(1));
        assertEquals(1, distributedPoints.get(1));
    }
    
}
