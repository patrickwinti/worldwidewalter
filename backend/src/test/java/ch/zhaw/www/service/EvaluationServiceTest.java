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
    private static final String Round_ID = "Round-ID";

    private Round mockRoundInRepository() {
        Round roundMock = mock(Round.class);
        when(roundMock.getPropositions()).thenReturn(List.of(
                createProposition("Player-ID-1", "orange"),
                createProposition("Sphinx-ID", "blue"),
                createProposition("Player-ID-3", "green")
        ));

        when(roundMock.getSphinx()).thenReturn(new Player("Sphinx-ID", "Sphinx-Name"));
        when(roundMock.getAtLeastOneNoneSphinxPropositionHasBeenSelected()).thenReturn(false);
        when(roundMock.getTempSphinxPoints()).thenReturn(1);

        return roundMock;
    }


    @Test
    public void evaluateSelection_shouldReturnPointsForSelectedProposition() {

        Round roundMock = mockRoundInRepository();
        String idOfSelectedProposition = roundMock.getPropositions().get(0).getPropositionId();
        String idOfPlayerWhoSelectedProposition = roundMock.getPropositions().get(2).getPlayerId();

        Map<String, Integer> result = evaluationService.evaluateSelection(roundMock, idOfSelectedProposition, idOfPlayerWhoSelectedProposition);

        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(1), result.get(idOfPlayerWhoSelectedProposition));
        assertEquals(Integer.valueOf(0), result.get("sphinxId"));
//        verify(roundMock.getAtLeastOneNoneSphinxPropositionHasBeenSelected(), times(1)).set(true);
    }


}
