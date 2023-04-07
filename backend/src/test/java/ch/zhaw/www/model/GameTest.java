package ch.zhaw.www.model;

import ch.zhaw.www.service.GameError;
import ch.zhaw.www.service.PlayerError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static ch.zhaw.www.TestHelper.*;
import static ch.zhaw.www.TimeHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameTest {
    @AfterEach
    void tearDown() {
        disableFixedClocked();
    }
    
    @Test
    void testGameState_WaitingForPlayers() {
        var game = createGame();
        assertEquals(Game.State.NO_VALID_ROUND, game.getState());
        
        game.addRound(createRound());
        
        var player1 = addWaitingRoomPlayer(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        var player2 = addWaitingRoomPlayer(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        var player3 = addWaitingRoomPlayer(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        var player4 = addWaitingRoomPlayer(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        game.moveToActivePlayers(player1);
        game.moveToActivePlayers(player2);
        game.moveToActivePlayers(player3);
        game.moveToActivePlayers(player4);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        
        Round round = createRound();
        game.addRound(round);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        
        round.setSphinx(getRandomPlayer(game));
        assertEquals(Game.State.WAITING_FOR_ALL_PROPOSITIONS, game.getState());
    }
    
    @Test
    void testGameState_WaitingForPropositions() {
        var game = createGame();
        game.addRound(createRound());
        
        int numberOfPlayersToAdd = 10;
        for (int i = 0; i < numberOfPlayersToAdd; i++) {
            addWaitingRoomPlayer(game);
        }
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        
        addRoundOpenForPropositionSubmission(game);
        assertEquals(Game.State.WAITING_FOR_ALL_PROPOSITIONS, game.getState());
        
        game.getAllPlayers().forEach(game::moveToActivePlayers);
        assertEquals(Game.State.WAITING_FOR_ALL_PROPOSITIONS, game.getState());
    }
    
    @Test
    void testGameState_WaitingForSelections() {
        var game = createGame();
        int numberOfPlayersToAdd = 10;
        for (int i = 0; i < numberOfPlayersToAdd; i++) {
            addActivePlayer(game);
        }
        
        addRoundOpenForPropositionSubmission(game);
        var round = game.getCurrentRound();
        assertNotNull(round);
        
        game.getAllPlayers().forEach(player -> round.addProposition(createProposition(player.getId(), "Walter " + player.getId())));
        assertEquals(Game.State.WAITING_FOR_ALL_SELECTIONS, game.getState());
        
        enableFixedClocked();
        addRoundOpenForPropositionSubmission(game);
        var player = getRandomPlayer(game);
        game.getCurrentRound().addProposition(createProposition(player.getId(), "Walter " + player.getId()));
        offsetFixedClockBy(DEFAULT_PROPOSITION_DURATION);
        assertEquals(Game.State.WAITING_FOR_ALL_SELECTIONS, game.getState());
    }
    
    @Test
    void testRunningRound() {
        var game = createGame();
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
        var game = createGame();
        assertNull(game.getCurrentRound());
        game.addRound(createRound());
        assertNotNull(game.getCurrentRound());
    }
    
    @Test
    void testMarkPlayerAsActive_NotFound() {
        var game = createGame();
        
        var unknonwPlayer = createPlayer();
        assertThrows(PlayerError.NotFoundException.class, () -> game.moveToActivePlayers(unknonwPlayer));
        assertFalse(game.hasActivePlayer(unknonwPlayer.getId()));
    }
    
    @Test
    void testMarkPlayerAsActive_AlreadyInActive() {
        var game = createGame();
        var player = createPlayer();
        game.addPlayerToWaitingRoom(player);
        game.moveToActivePlayers(player);
        
        assertTrue(game.hasActivePlayer(player.getId()));
        game.moveToActivePlayers(player);
        assertTrue(game.hasActivePlayer(player.getId()));
    }
    
    @Test
    void testMarkPlayerAsActive_TooMayActivePlayers() {
        var game = createGame();
        for (int i = 0; i < MAX_NUMBER_OF_PLAYERS; i++) {
            final Player player = createPlayer();
            game.addPlayerToWaitingRoom(player);
            game.moveToActivePlayers(player);
        }
        var onPlayerTooMuch = createPlayer();
        game.addPlayerToWaitingRoom(onPlayerTooMuch);
        
        assertThrows(GameError.FullCapacityException.class, () -> game.moveToActivePlayers(onPlayerTooMuch));
        assertFalse(game.hasActivePlayer(onPlayerTooMuch.getId()));
    }
    
    @Test
    void testMarkPlayerAsActive_InWaitingRoom() {
        var game = createGame();
        game.addPlayerToWaitingRoom(createPlayer());
        var player = createPlayer();
        game.addPlayerToWaitingRoom(player);
        
        assertFalse(game.hasActivePlayer(player.getId()));
        game.moveToActivePlayers(player);
        assertTrue(game.hasActivePlayer(player.getId()));
    }
    
}