package ch.zhaw.www.service;

import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.zhaw.www.TestHelper.createProposition;
import static ch.zhaw.www.TestHelper.createRound;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@SpringBootTest
public class EvaluationServiceTest {


    private EvaluationService evaluationService;
    @MockBean
    private final EntityService entityService = mock(EntityService.class);
    private static final String Round_ID = "Round-ID";

    private Round mockRoundInRepository() {
        Round roundMock = mock(Round.class);
        when(entityService.getRound(eq(Round_ID))).thenReturn(roundMock);
        when(roundMock.getPropositions()).thenReturn(List.of(
                createProposition("Player-ID-1", "orange"),
                createProposition("Sphinx-ID", "blue"),
                createProposition("Player-ID-3", "green")
        ));
        when(roundMock.getSphinx()).thenReturn(new Player("sphinxId", "sphinxName"));
        when(roundMock.getAtLeastOneNoneSphinxPropositionHasBeenSelected()).thenReturn(
        when(roundMock.getTempSphinxPoints()).thenReturn();

        return roundMock;
    }

    @Test
    public void evaluateSelection_shouldReturnPointsForSelectedProposition() {

        Round roundMock = mockRoundInRepository();
        String idOfSelectedProposition = roundMock.getPropositions().get(0).getPropositionId();
        String idOfPlayerWhoSelectedProposition = roundMock.getPropositions().get(2).getPlayerId();

        Map<String, Integer> result = evaluationService.evaluateSelection(Round_ID, idOfSelectedProposition, roundMock.getPropositions(),);

        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(1), result.get(idOfPlayerWhoSelectedProposition));
        assertEquals(Integer.valueOf(0), result.get("sphinxId"));
        verify(roundMock.getAtLeastOneNoneSphinxPropositionHasBeenSelected(), times(1)).set(true);
    }


}
