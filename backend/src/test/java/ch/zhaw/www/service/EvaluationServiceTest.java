package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Proposition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static ch.zhaw.www.TestHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest
public class EvaluationServiceTest {
    private static final String GAME_ID = "GAME ID";

    @Autowired
    private GameService gameService;
    @MockBean
    private EntityService entityService;
    private EvaluationService evaluationService;

//    @Test
    //TODO
//    public void testEvaluateRound() {
//        Game game = mockGameInRepository();
//        List<Proposition> propositions = new ArrayList<>();
//        propositions.add(createProposition("1", "orange"));
//        propositions.add(createProposition("2", "blue"));
//        propositions.add(createProposition("3", "green"));
//        Map<String, String> selections = new HashMap<>();
//        selections.put(propositions.get(0).getPlayerId(), propositions.get(0).getId());
//        selections.put(propositions.get(1).getPlayerId(), propositions.get(1).getId());
//        selections.put(propositions.get(2).getPlayerId(), propositions.get(2).getId());
//
//        evaluationService.evaluateRound(game.getId());

//    }
    private Game mockGameInRepository() {
        var game = createGame(GAME_ID);

        doAnswer(invocationOnMock -> {
            var lambda = invocationOnMock.getArgument(1, Consumer.class);
            //noinspection unchecked
            lambda.accept(game);
            return null;
        }).when(entityService).editGame(eq(game.getId()), any());
        when(entityService.getGame(game.getId())).thenReturn(game);

        return game;
    }
}
