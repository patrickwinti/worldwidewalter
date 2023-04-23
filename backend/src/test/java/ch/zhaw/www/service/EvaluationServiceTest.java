package ch.zhaw.www.service;

import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static ch.zhaw.www.TestHelper.createDoubleSubmissionProposition;
import static ch.zhaw.www.TestHelper.createProposition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        when(roundMock.isHasNonSphinxPropositionBeenSelected()).thenReturn(atLeastOneNoneSphinxPropositionHasBeenSelected);
        when(roundMock.getTempSphinxPoints()).thenReturn(tempSphinxPoints);

        return roundMock;
    }
    private Round mockRoundWithSphinxDoublePropositionInRepository(int tempSphinxPoints, boolean atLeastOneNoneSphinxPropositionHasBeenSelected) {
        Round roundMock = mock(Round.class);
        when(roundMock.getPropositions()).thenReturn(List.of(
                createProposition("Player-ID-1", "orange"),
                createProposition("Player-ID-2", "green"),
                createProposition("Player-ID-3", "red"),
                createDoubleSubmissionProposition("Player-ID-4", sphinxId, "blue")

        ));

        when(roundMock.getSphinx()).thenReturn(new Player(sphinxId, "Sphinx-Name"));
        when(roundMock.isHasNonSphinxPropositionBeenSelected()).thenReturn(atLeastOneNoneSphinxPropositionHasBeenSelected);
        when(roundMock.getTempSphinxPoints()).thenReturn(tempSphinxPoints);

        return roundMock;
    }
    private Round mockRoundWithoutSphinxDoublePropositionInRepository(int tempSphinxPoints, boolean atLeastOneNoneSphinxPropositionHasBeenSelected) {
        Round roundMock = mock(Round.class);
        when(roundMock.getPropositions()).thenReturn(List.of(
                createDoubleSubmissionProposition("Player-ID-1","Player-ID-3" , "water"),
                createProposition("Player-ID-2", "cognac"),
                createProposition(sphinxId, "tea")

        ));

        when(roundMock.getSphinx()).thenReturn(new Player(sphinxId, "Sphinx-Name"));
        when(roundMock.isHasNonSphinxPropositionBeenSelected()).thenReturn(atLeastOneNoneSphinxPropositionHasBeenSelected);
        when(roundMock.getTempSphinxPoints()).thenReturn(tempSphinxPoints);

        return roundMock;
    }

    @Test
    void evaluateSelectionOfNonSphinxPropositionWithTempSpinxPoints() {

        Round roundMock = mockRoundInRepository(3, false);
        String idOfSelectedProposition = roundMock.getPropositions().get(0).getId();
        String propositionOriginatorId = roundMock.getPropositions().get(0).getPlayerIds().get(0);
        String selectorId = roundMock.getPropositions().get(2).getPlayerIds().get(0);

        Map<String, Integer> evaluation = evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, selectorId);
        List<Integer> distributedPoints = List.copyOf(evaluation.values());
        List<String> playerIds = List.copyOf(evaluation.keySet());
        verify(roundMock).setTempSphinxPoints(0);
        verify(roundMock).setHasNonSphinxPropositionBeenSelected(true);

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
        String selectorId = roundMock.getPropositions().get(2).getPlayerIds().get(0);

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
        String selectorId = roundMock.getPropositions().get(2).getPlayerIds().get(0);

        Map<String, Integer> evaluation = evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, selectorId);
        List<Integer> distributedPoints = List.copyOf(evaluation.values());
        List<String> playerIds = List.copyOf(evaluation.keySet());

        assertEquals(2, evaluation.size());

        assertEquals(selectorId, playerIds.get(0));
        assertEquals(1, distributedPoints.get(0));

        assertEquals(sphinxId, playerIds.get(1));
        assertEquals(1, distributedPoints.get(1));
    }

    @Test
    void evaluateDuplicatePropositionSpinxSelected() {
        Round roundMock = mockRoundWithSphinxDoublePropositionInRepository(0, true);

        String idOfSelectedProposition = roundMock.getPropositions().get(3).getId();
        String selectorId = roundMock.getPropositions().get(3).getPlayerIds().get(0);

        Map<String, Integer> evaluation = evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, selectorId);
        verify(roundMock, times (0)).setTempSphinxPoints(anyInt());
        List<Integer> distributedPoints = List.copyOf(evaluation.values());
        List<String> playerIds = List.copyOf(evaluation.keySet());

        assertEquals(1, evaluation.size());

        assertEquals(selectorId, playerIds.get(0));
        assertEquals(1, distributedPoints.get(0));

    }

    @Test
    void evaluateSelectorHasSubmittedDuplicatePropositionAndSelectedUniqueNonSphinxProposition() {
        Round roundMock = mockRoundWithSphinxDoublePropositionInRepository(3, true);

        String idOfSelectedProposition = roundMock.getPropositions().get(2).getId();
        String selectorId = roundMock.getPropositions().get(3).getPlayerIds().get(0);
        String proposerId = roundMock.getPropositions().get(2).getPlayerIds().get(0);

        Map<String, Integer> evaluation = evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, selectorId);
        verify(roundMock).getTempSphinxPoints();
        verify(roundMock).setTempSphinxPoints(0);
        List<Integer> distributedPoints = List.copyOf(evaluation.values());
        List<String> playerIds = List.copyOf(evaluation.keySet());

        assertEquals(2, evaluation.size());

        assertEquals(proposerId, playerIds.get(0));
        assertEquals(1, distributedPoints.get(0));
        assertEquals(sphinxId, playerIds.get(1));
        assertEquals(3, distributedPoints.get(1));
    }
    @Test
    void evaluateSelectorHasSubmittedDuplicatePropositionAndSelectedDuplicateNonSphinxProposition() {
        Round roundMock = mockRoundWithoutSphinxDoublePropositionInRepository(3, true);

        String selectorId = roundMock.getPropositions().get(0).getPlayerIds().get(0);
        String idOfSelectedProposition = roundMock.getPropositions().get(0).getId();
        String proposerId = roundMock.getPropositions().get(0).getPlayerIds().get(1);

        Map<String, Integer> evaluation = evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, selectorId);
        verify(roundMock).getTempSphinxPoints();
        verify(roundMock).setTempSphinxPoints(0);
        List<Integer> distributedPoints = List.copyOf(evaluation.values());
        List<String> playerIds = List.copyOf(evaluation.keySet());

        assertEquals(1, evaluation.size());

        assertEquals(sphinxId, playerIds.get(0));
        assertEquals(3, distributedPoints.get(0));
    }
    @Test
    void evaluateIllegalSelection(){
        Round roundMock = mockRoundInRepository(0, false);
        String idOfSelectedProposition = roundMock.getPropositions().get(2).getId();
        String selectorId = roundMock.getPropositions().get(2).getPlayerIds().get(0);

        assertThrows(SelectionError.IllegalSelection.class, () -> evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, selectorId));
    }

}
