package ch.zhaw.www.model;

import ch.zhaw.www.utils.InstantWrapper;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static ch.zhaw.www.model.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameTest {
    
    private static final int DURATION = 4;
    
    private static void addRoundOpenForPropositionSubmission(Game game) {
        Round round = getRound(DURATION);
        game.addRound(round);
        game.getActivePlayers().putAll(game.getWaitingRoom());
        round.setSphinx(game.getActivePlayers().values().iterator().next());
    }
    
    @Test
    void testGameState_WaitingForPlayers() {
        Game game = getGame();
        assertEquals(Game.State.NO_VALID_ROUND, game.getState());
        
        game.addRound(getRound(DURATION));
        
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        
        game.getActivePlayers().putAll(game.getWaitingRoom());
        assertEquals(Game.State.NO_VALID_ROUND, game.getState());
        
        Round round = getRound(DURATION);
        game.addRound(round);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        
        round.setSphinx(game.getWaitingRoom().values().iterator().next());
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
    }
    
    @Test
    void testGameState_WaitingForPropositions() {
        Game game = getGame();
        game.addRound(getRound(DURATION));
        
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
        Game game = getGame();
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
        InstantWrapper.clock = Clock.offset(InstantWrapper.clock, Duration.of(DURATION, ChronoUnit.MINUTES));
        assertEquals(Game.State.WAITING_FOR_ALL_SELECTIONS, game.getState());
    }
    
    @Test
    void testRunningRound() {
        Game game = getGame();
        assertNull(game.getCurrentRound());
        
        Round round = mock(Round.class);
        when(round.getState()).thenReturn(Round.State.CREATED);
        game.addRound(round);
        assertSame(round, game.getCurrentRound());
        
        when(round.getState()).thenReturn(Round.State.OPEN_FOR_SUBMISSIONS);
        Player sphinx = getPlayer();
        round.setSphinx(sphinx);
        assertSame(round, game.getCurrentRound());
        
        when(round.getState()).thenReturn(Round.State.FINISHED);
        assertNull(game.getCurrentRound());
        
    }
    
    @Test
    void testNewRound() {
        Game game = getGame();
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        assertEquals(4, game.getWaitingRoom().size());
        addToActive(game);
        addToActive(game);
        addToActive(game);
        addToActive(game);
        assertEquals(4, game.getActivePlayers().size());
        
        game.addRound(getRound(DURATION));
        
        assertEquals(8, game.getWaitingRoom().size());
        assertEquals(0, game.getActivePlayers().size());
    }
}