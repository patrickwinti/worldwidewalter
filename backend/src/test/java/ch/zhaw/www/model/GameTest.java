package ch.zhaw.www.model;

import ch.zhaw.www.utils.InstantWrapper;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static ch.zhaw.www.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameTest {
    
    @Test
    void testGameState_WaitingForPlayers() {
        Game game = createGame();
        assertEquals(Game.State.NO_VALID_ROUND, game.getState());
        
        game.addRound(createRound());
        
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        
        game.getActivePlayers().putAll(game.getWaitingRoom());
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        
        Round round = createRound();
        game.addRound(round);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        
        round.setSphinx(getRandomPlayer(game));
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
    }
    
    @Test
    void testGameState_WaitingForPropositions() {
        Game game = createGame();
        game.addRound(createRound());
        
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        
        addRoundOpenForPropositionSubmission(game);
        assertEquals(Game.State.WAITING_FOR_ALL_PROPOSITIONS, game.getState());
    }
    
    @Test
    void testGameState_WaitingForSelections() {
        Game game = createGame();
        addToActive(game);
        addToActive(game);
        addToActive(game);
        addToActive(game);
        
        addRoundOpenForPropositionSubmission(game);
        var round = game.getCurrentRound();
        assertNotNull(round);
        game.getActivePlayers().forEach((s, player) -> round.getPropositions().put(s, List.of("Walter " + player.getId())));
        assertEquals(Game.State.WAITING_FOR_ALL_SELECTIONS, game.getState());
        
        InstantWrapper.clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        addRoundOpenForPropositionSubmission(game);
        var player = game.getActivePlayers().values().stream().findAny().get();
        game.getCurrentRound().getPropositions().put(player.getId(), List.of("Walter " + player.getId()));
        InstantWrapper.clock = Clock.offset(InstantWrapper.clock, DEFAULT_PROPOSITION_DURATION);
        assertEquals(Game.State.WAITING_FOR_ALL_SELECTIONS, game.getState());
    }
    
    @Test
    void testRunningRound() {
        Game game = createGame();
        assertNull(game.getCurrentRound());
        
        Round round = mock(Round.class);
        when(round.getState()).thenReturn(Round.State.CREATED);
        game.addRound(round);
        assertSame(round, game.getCurrentRound());
        
        when(round.getState()).thenReturn(Round.State.OPEN_FOR_SUBMISSIONS);
        Player sphinx = createPlayer();
        round.setSphinx(sphinx);
        assertSame(round, game.getCurrentRound());
        
        when(round.getState()).thenReturn(Round.State.FINISHED);
        assertNull(game.getCurrentRound());
        
    }
    
    @Test
    void testNewRound() {
        Game game = createGame();
        assertEquals(0, game.getRounds().size());
        game.addRound(createRound());
        assertEquals(1, game.getRounds().size());
    }
    
}