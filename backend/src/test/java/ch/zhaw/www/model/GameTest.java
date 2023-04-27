package ch.zhaw.www.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        IntStream.range(0, numberOfPlayersToAdd).forEach(value -> addWaitingRoomPlayer(game));
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        
        addRoundOpenForPropositionSubmission(game); //Round with Sphinx but not enough active players
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        
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
    }
    
    @Test
    void testNewRound() {
        var game = createGame();
        assertNull(game.getCurrentRound());
        
        IntStream.range(0, MAX_NUMBER_OF_PLAYERS).forEach(i -> game.addPlayerToWaitingRoom(createPlayer()));
        final Round round1 = createRound();
        game.addRound(round1);
        assertNotNull(game.getCurrentRound());
        
        var doesNotFitPlayer = createPlayer();
        game.addPlayerToWaitingRoom(doesNotFitPlayer);
        
        final Round round2 = createRound();
        game.addRound(round2);
        assertNotEquals(round1, game.getCurrentRound());
        assertEquals(round2, game.getCurrentRound());
        assertFalse(game.hasActivePlayer(doesNotFitPlayer.getId()));
    }
    
    @Test
    void testMarkPlayerAsActive_NotFound() {
        var game = createGame();
        
        var unknonwPlayer = createPlayer();
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
    
    @Test
    void consumePrompts() {
        List<Prompt> originalList = List.of(new Prompt("WALTER WALTER WALTEROO", 2),
                new Prompt("WALTER WALTER hello", 1),
                new Prompt("WALTER says hi", 1));
        
        Game game = new Game("12345", 4, 10, 1, originalList);
        
        var prompt1 = game.consumePrompt();
        assertEquals(prompt1, originalList.get(0));
        var prompt2 = game.consumePrompt();
        assertEquals(prompt2, originalList.get(0));
        
        game.addRound(createRound());
        var prompt3 = game.consumePrompt();
        assertEquals(prompt3, originalList.get(1));
        var prompt4 = game.consumePrompt();
        assertNotEquals(prompt4, originalList.get(2));
        
        game.addRound(createRound());
        var prompt5 = game.consumePrompt();
        assertEquals(prompt5, originalList.get(2));
        game.addRound(createRound());
        
        var prompt6 = game.consumePrompt();
        assertEquals(prompt6, originalList.get(0));
    }
    
    @Test
    void addPoints() {
        var player1 = "player1";
        var player2 = "player2";
        var player3 = "player3";
        Map<String, Integer> evaluation = new HashMap<>();
        evaluation.put(player1, 2);
        evaluation.put(player2, 1);
        Game game = new Game("12345", 4, 10, 1, List.of());
        
        game.addPoints(evaluation);
        assertTrue(game.getPoints().containsKey(player1));
        assertEquals(2, game.getPoints().get(player1));
        assertTrue(game.getPoints().containsKey(player2));
        assertEquals(1, game.getPoints().get(player2));
        
        Map<String, Integer> evaluation2 = new HashMap<>();
        evaluation2.put(player2, 1);
        evaluation2.put(player3, 1);
        game.addPoints(evaluation2);
        
        assertTrue(game.getPoints().containsKey(player1));
        assertEquals(2, game.getPoints().get(player1));
        assertTrue(game.getPoints().containsKey(player2));
        assertEquals(2, game.getPoints().get(player2));
        assertTrue(game.getPoints().containsKey(player3));
        assertEquals(1, game.getPoints().get(player3));
    }
    
}