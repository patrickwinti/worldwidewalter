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
public class EvaluationServiceTest {


    private EvaluationService evaluationService = new EvaluationServiceImpl();
    private static final String SphinxId = "Sphinx-ID";

    private Round mockRoundInRepository(int tempSphinxPoints, boolean atLeastOneNoneSphinxPropositionHasBeenSelected) {
        Round roundMock = mock(Round.class);
        when(roundMock.getPropositions()).thenReturn(List.of(
                createProposition("Player-ID-1", "orange"),
                createProposition("Player-ID-2", "green"),
                createProposition("Player-ID-3", "red"),
                createProposition(SphinxId, "blue")
                ));

        when(roundMock.getSphinx()).thenReturn(new Player(SphinxId, "Sphinx-Name"));
        when(roundMock.getAtLeastOneNoneSphinxPropositionHasBeenSelected()).thenReturn(atLeastOneNoneSphinxPropositionHasBeenSelected);
        when(roundMock.getTempSphinxPoints()).thenReturn(tempSphinxPoints);

        return roundMock;
    }


    @Test
    public void evaluateSelectionOfNonSphinxPropositionWithTempSpinxPoints() {

        Round roundMock = mockRoundInRepository(3, false);
        String idOfSelectedProposition = roundMock.getPropositions().get(0).getPropositionId();
        String selectorId = roundMock.getPropositions().get(2).getPlayerId();

        Map<String, Integer> result = evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, selectorId);
        List<Integer> distributedPoints = List.copyOf(result.values());
        List<String> PlayerIds = List.copyOf(result.keySet());

        assertEquals(2, result.size());

        assertEquals(roundMock.getPropositions().get(0).getPlayerId(), PlayerIds.get(0));
        assertEquals(1, distributedPoints.get(0));

        assertEquals(SphinxId, PlayerIds.get(1));
        assertEquals(3, distributedPoints.get(1));

    }

    @Test
    public void evaluateSelectionOfOnlySphinxPropositions() {

        Round roundMock = mockRoundInRepository(3,false);
        String idOfSelectedProposition = roundMock.getPropositions().get(3).getPropositionId();
        String selectorId = roundMock.getPropositions().get(2).getPlayerId();

        Map<String, Integer> result = evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, selectorId);
        List<Integer> distributedPoints = List.copyOf(result.values());
        List<String> PlayerIds = List.copyOf(result.keySet());


        assertEquals(1, result.size());

        assertEquals(selectorId, PlayerIds.get(0));
        assertEquals(1, distributedPoints.get(0));
    }

    @Test
    public void evaluateSelectionOfSphinxPropositionWithNoTempSpinxPoints() {

            Round roundMock = mockRoundInRepository(0, true);
            String idOfSelectedProposition = roundMock.getPropositions().get(3).getPropositionId();
            String selectorId = roundMock.getPropositions().get(2).getPlayerId();

            Map<String, Integer> result = evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, selectorId);
            List<Integer> distributedPoints = List.copyOf(result.values());

            assertEquals(2, result.size());
            assertEquals(1, distributedPoints.get(0));
            assertEquals(1, distributedPoints.get(1));
    }


}
