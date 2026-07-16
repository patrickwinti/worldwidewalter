package ch.zhaw.www.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
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
        assertTrue(game.needsNewRound());
        
        game.addRound(createRound());
        
        var player1 = registerPlayer(game);
        assertTrue(game.canRoundBeEntered());
        var player2 = registerPlayer(game);
        assertTrue(game.canRoundBeEntered());
        var player3 = registerPlayer(game);
        assertTrue(game.canRoundBeEntered());
        var player4 = registerPlayer(game);
        assertTrue(game.canRoundBeEntered());
        game.moveToActivePlayers(player1);
        game.moveToActivePlayers(player2);
        game.moveToActivePlayers(player3);
        game.moveToActivePlayers(player4);
        assertTrue(game.canRoundBeEntered());
        
        Round round = createRound();
        game.addRound(round);
        assertTrue(game.canRoundBeEntered());
        
        assertFalse(game.canAcceptPropositionsForCurrentRound());
    }
    
    @Test
    void testGameState_WaitingForPropositions() {
        var game = createGame();
        game.addRound(createRound());
        
        int numberOfPlayersToAdd = 10;
        IntStream.range(0, numberOfPlayersToAdd).forEach(value -> registerPlayer(game));
        assertTrue(game.canRoundBeEntered());
        
        addRoundOpenForPropositionSubmission(game, numberOfPlayersToAdd); //Round with Sphinx but not enough active players
        assertTrue(game.canRoundBeEntered());
        
        game.getAllPlayers().forEach(game::moveToActivePlayers);
        assertTrue(game.canAcceptPropositionsForCurrentRound());
    }
    
    @Test
    void testGameState_WaitingForSelections() {
        var game = createGame();
        int numberOfPlayersToAdd = 10;
        addRoundOpenForPropositionSubmission(game, numberOfPlayersToAdd);
        var round = game.getCurrentRound();
        assertNotNull(round);
        
        game.getAllPlayers().forEach(player -> round.addProposition(createProposition(player.getId(), player.getId())));
        assertFalse(game.canAcceptPropositionsForCurrentRound());
        assertTrue(game.canAcceptSelectionForRound(round));
        
        enableFixedClocked();
        addRoundOpenForPropositionSubmission(game, numberOfPlayersToAdd);
        var player = getRandomPlayer(game);
        game.getCurrentRound().addProposition(createProposition(player.getId(), "Walter " + player.getId()));
        offsetFixedClockBy(DEFAULT_PROPOSITION_DURATION);
        assertFalse(game.canAcceptPropositionsForCurrentRound());
        assertTrue(game.canAcceptSelectionForRound(game.getCurrentRound()
        ));
    }
    
    @Test
    void testRoundWaitsForAllPresentPlayersBeforeSelection() {
        var game = createGame();
        addRoundOpenForPropositionSubmission(game, 4); // four active players + sphinx
        var round = game.getCurrentRound();
        var players = game.getAllPlayers().toList();

        // Only three of the four present players have submitted a proposition.
        players.stream().limit(3).forEach(p -> round.addProposition(createProposition(p.getId(), p.getId())));
        assertFalse(game.canAcceptSelectionForRound(round),
                "selection must not open until every present player has submitted");

        // A brief disconnect of the missing player must NOT let the round advance without them.
        game.markPlayerDisconnected(players.get(3).getId());
        assertFalse(game.canAcceptSelectionForRound(round),
                "a briefly disconnected player must still be waited for");

        // Once the fourth player submits too, selection opens.
        round.addProposition(createProposition(players.get(3).getId(), players.get(3).getId()));
        assertTrue(game.canAcceptSelectionForRound(round));
    }

    @Test
    void testLeavingPlayerNoLongerBlocksSelection() {
        var game = createGame();
        addRoundOpenForPropositionSubmission(game, 4);
        var round = game.getCurrentRound();
        var players = game.getAllPlayers().toList();

        players.stream().limit(3).forEach(p -> round.addProposition(createProposition(p.getId(), p.getId())));
        assertFalse(game.canAcceptSelectionForRound(round));

        // The fourth player leaves the game entirely and must no longer block the round.
        game.removePlayer(players.get(3).getId());
        assertTrue(game.canAcceptSelectionForRound(round),
                "a player who left must not block the round");
    }

    @Test
    void testRoundAdvancesAfterDisconnectGraceExpires() {
        enableFixedClocked();
        var game = createGame();
        addRoundOpenForPropositionSubmission(game, 4);
        var round = game.getCurrentRound();
        var players = game.getAllPlayers().toList();

        // Three of four present players submitted; the fourth then loses connection.
        players.stream().limit(3).forEach(p -> round.addProposition(createProposition(p.getId(), p.getId())));
        game.markPlayerDisconnected(players.get(3).getId());

        // Within the grace period the round still waits for the disconnected player.
        assertFalse(game.canAcceptSelectionForRound(round),
                "a briefly disconnected player must still be waited for");

        // Once the disconnect exceeds the grace period, they are ignored and selection opens.
        offsetFixedClockBy(Duration.ofSeconds(11));
        assertTrue(game.canAcceptSelectionForRound(round),
                "a player gone longer than the grace period must not block the round");
    }

    @Test
    void testReconnectClearsDisconnectGrace() {
        enableFixedClocked();
        var game = createGame();
        addRoundOpenForPropositionSubmission(game, 4);
        var round = game.getCurrentRound();
        var players = game.getAllPlayers().toList();
        players.stream().limit(3).forEach(p -> round.addProposition(createProposition(p.getId(), p.getId())));

        game.markPlayerDisconnected(players.get(3).getId());
        game.markPlayerConnected(players.get(3).getId()); // socket recovered before the grace expired
        offsetFixedClockBy(Duration.ofSeconds(11));        // well past the original drop

        // Because they reconnected, the grace timer was cleared, so they are still waited for.
        assertFalse(game.canAcceptSelectionForRound(round),
                "a reconnected player must still be waited for");
    }

    @Test
    void testRunningRound() {
        var game = createGame();
        assertNull(game.getCurrentRound());
        
        Round round = mock(Round.class);
        game.addRound(round);
        assertSame(round, game.getCurrentRound());
        
        when(round.acceptsPropositions()).thenReturn(true);
        Player sphinx = createPlayer();
        round.setSphinx(sphinx);
        assertSame(round, game.getCurrentRound());
    }
    
    @Test
    void testNewRound() {
        var game = createGame();
        assertNull(game.getCurrentRound());
        
        IntStream.range(0, MAX_NUMBER_OF_PLAYERS).forEach(i -> game.registerPlayer(createPlayer()));
        final Round round1 = createRound();
        game.addRound(round1);
        assertNotNull(game.getCurrentRound());
        
        var doesNotFitPlayer = createPlayer();
        game.registerPlayer(doesNotFitPlayer);
        
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
        var player1 = createPlayer();
        game.registerPlayer(player1);
        game.moveToActivePlayers(player1);
        
        assertTrue(game.hasActivePlayer(player1.getId()));
        var player2 = createPlayer();
        game.registerPlayer(player2);
        game.moveToActivePlayers(player2);
        
        var candidates = game.getSphinxCandidates();
        assertEquals(2, candidates.size());
        
        candidates.remove(game.getSphinxCandidates().keySet().stream().findFirst().get());
        game.setSphinxCandidates(candidates);
        
        game.moveToActivePlayers(player1);
        game.moveToActivePlayers(player2);
        assertTrue(game.hasActivePlayer(player1.getId()));
        assertTrue(game.hasActivePlayer(player2.getId()));
        assertEquals(1, game.getSphinxCandidates().size());
    }
    
    @Test
    void testMarkPlayerAsActive_TooMayActivePlayers() {
        var game = createGame();
        for (int i = 0; i < MAX_NUMBER_OF_PLAYERS; i++) {
            final Player player = createPlayer();
            game.registerPlayer(player);
            game.moveToActivePlayers(player);
        }
        var onPlayerTooMuch = createPlayer();
        game.registerPlayer(onPlayerTooMuch);
        
        assertFalse(game.hasActivePlayer(onPlayerTooMuch.getId()));
    }
    
    @Test
    void testMarkPlayerAsActive_InWaitingRoom() {
        var game = createGame();
        game.registerPlayer(createPlayer());
        var player = createPlayer();
        game.registerPlayer(player);
        
        assertFalse(game.hasActivePlayer(player.getId()));
        game.moveToActivePlayers(player);
        assertTrue(game.hasActivePlayer(player.getId()));
    }
    
    @Test
    void consumePrompts() {
        List<Prompt> originalList = List.of(new Prompt(WALTER_MARKER + " " + WALTER_MARKER + " WALTEROO", List.of("WALTER", "WALTER")),
                new Prompt(WALTER_MARKER + " " + WALTER_MARKER + " hello", List.of("WALTER", "WALTER")),
                new Prompt(WALTER_MARKER + " says hi", List.of("WALTER")));
        
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
    
    @Test
    void testGetPlayerNameFromId() {
        Game game = createGame();
        Player manta = createPlayer("Manta");
        Player hai = createPlayer("Hai");
        game.registerPlayer(manta);
        game.registerPlayer(hai);
        assertEquals("Manta", game.getPlayerNameFromId(manta.getId()));
        assertEquals("Hai", game.getPlayerNameFromId(hai.getId()));
        assertNull(game.getPlayerNameFromId("does not exist"));
    }
    
    @Test
    void testSphinxCandidates() {
        Game game = createGame();
        Player flower = createPlayer("Flower");
        Player power = createPlayer("Power");
        game.registerPlayer(flower);
        game.registerPlayer(power);
        game.setSphinxCandidates(Map.of());
        assertEquals(2, game.getSphinxCandidates().size());
        assertEquals(game.getSphinxCandidates().get(flower), game.getSphinxCandidates().get(power));
        assertEquals(NUMBER_OF_ROUNDS_PER_TURN, game.getSphinxCandidates().get(power));
    }
    
    private void addRoundOpenForPropositionSubmission(Game game, int numberOfPlayersToAdd) {
        Round round = createRound();
        game.addRound(round);
        for (int i = 0; i < numberOfPlayersToAdd; i++) {
            addActivePlayer(game);
        }
        round.setSphinx(getRandomPlayer(game));
    }
    
}