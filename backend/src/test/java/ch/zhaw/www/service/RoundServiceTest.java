package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static ch.zhaw.www.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest
class RoundServiceTest {
    private static final String GAME_ID = "GAME ID";
    
    @Autowired
    private RoundService roundService;
    @MockBean
    private EntityService entityService;
    
    @Test
    void addPropositionThatAlreadyExists() {
        var game = mockRoundInRepository();
        var players = List.of(createPlayer(), createPlayer(), createPlayer());
        players.forEach(player -> {
            game.addPlayerToWaitingRoom(player);
            game.moveToActivePlayers(player);
        });
        var round = Objects.requireNonNull(game.getCurrentRound());
        roundService.submitProposition(round.getId(), players.get(0).getId(), List.of("Wasser", "Gummi"));
        roundService.submitProposition(round.getId(), players.get(1).getId(), List.of("WASSER", "gummi"));
        roundService.submitProposition(round.getId(), players.get(2).getId(), List.of("Wasser ", "  gummi"));
        assertEquals(1, round.getPropositions().size());
        assertEquals(2, round.getPropositions().get(0).getDuplicates().size());
        assertEquals(players.get(1).getId(), round.getPropositions().get(0).getDuplicates().get(0).getPlayerId());
        assertEquals(players.get(2).getId(), round.getPropositions().get(0).getDuplicates().get(1).getPlayerId());
    }
    
    private Game mockRoundInRepository() {
        var game = createGame(GAME_ID);
        var round = createRound();
        round.setSphinx(createPlayer());
        game.addRound(round);
        doAnswer(invocationOnMock -> {
            var lambda = invocationOnMock.getArgument(1, Consumer.class);
            //noinspection unchecked
            lambda.accept(game);
            return null;
        }).when(entityService).editRound(eq(round.getId()), any());
        when(entityService.getRound(round.getId())).thenReturn(round);
        return game;
        
    }
    
}