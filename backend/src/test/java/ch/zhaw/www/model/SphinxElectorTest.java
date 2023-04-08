package ch.zhaw.www.model;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.zhaw.www.TestHelper.createPlayer;
import static ch.zhaw.www.TestHelper.createRound;
import static org.junit.jupiter.api.Assertions.*;

class SphinxElectorTest {
    @Test
    void testWhenSelectorHasNoPlayers() {
        List<Round> rounds = new ArrayList<>();
        Map<String, Player> activePlayers = new HashMap<>();
        
        SphinxElector sphinxElector = new SphinxElector(rounds, activePlayers);
        assertNull(sphinxElector.selectCandidate(0));
        assertNull(sphinxElector.selectCandidate(1));
    }
    
    @Test
    void testWhenSelectHasNoCandidatesTakeFormActivePlayers() {
        List<Round> rounds = new ArrayList<>();
        Map<String, Player> activePlayers = Map.of("1", createPlayer());
        
        SphinxElector sphinxElector = new SphinxElector(rounds, activePlayers);
        assertNotNull(sphinxElector.selectCandidate(1));
    }
    
    @Test
    void testCandidatesAreReadFromRounds() {
        var currentSphinx = createPlayer("Sphinx");
        var round1 = createRound();
        var round2 = createRound();
        round1.setSphinx(currentSphinx);
        List<Round> rounds = List.of(round1, round2);
        Map<String, Player> activePlayers = createActivePlayers(2);
        activePlayers.put(currentSphinx.getId(), currentSphinx);
        
        SphinxElector sphinxElector = new SphinxElector(rounds, activePlayers);
        assertSame(currentSphinx, sphinxElector.selectCandidate(2));
    }
    
    @Test
    void testCandidatesAreReadFromRounds_alreadySet() {
        var currentSphinx = createPlayer("Sphinx");
        var round1 = createRound();
        var round2 = createRound();
        round1.setSphinx(currentSphinx);
        round2.setSphinx(currentSphinx);
        List<Round> rounds = List.of(round1, round2);
        Map<String, Player> activePlayers = createActivePlayers(2);
        activePlayers.put(currentSphinx.getId(), currentSphinx);
        
        SphinxElector sphinxElector = new SphinxElector(rounds, activePlayers);
        assertSame(currentSphinx, sphinxElector.selectCandidate(2));
    }
    
    @Test
    void testNewRound_SelectNewSphinx() {
        List<Round> rounds = new ArrayList<>();
        Map<String, Player> activePlayers = createActivePlayers(3);
        SphinxElector sphinxElector = new SphinxElector(rounds, activePlayers);
        final int numberOfRoundsInTurn = 2;
        var round1 = createRound();
        rounds.add(round1);
        var sphinx = sphinxElector.selectCandidate(numberOfRoundsInTurn);
        round1.setSphinx(sphinx);
        var round2 = createRound();
        rounds.add(round2);
        assertSame(sphinx, sphinxElector.selectCandidate(numberOfRoundsInTurn));
        round2.setSphinx(sphinx);
        rounds.add(createRound());
        assertNotSame(sphinx, sphinxElector.selectCandidate(numberOfRoundsInTurn));
    }
    
    @Test
    void testRemovingCandidate_InQueue() {
        List<Round> rounds = new ArrayList<>();
        final int numberOfPlayers = 10;
        Map<String, Player> activePlayers = createActivePlayers(numberOfPlayers);
        SphinxElector sphinxElector = new SphinxElector(rounds, activePlayers);
        
        final int numberOfRoundsInTurn = 1;
        
        rounds.add(createRound());
        // loads all players from activePlayers
        var sphinxForRound1 = sphinxElector.selectCandidate(numberOfRoundsInTurn);
        @SuppressWarnings("OptionalGetWithoutIsPresent") final String removedPlayerId = activePlayers.values().stream().filter(player -> !player.equals(sphinxForRound1)).findAny().get().getId();
        sphinxElector.removeCandidate(removedPlayerId);
        activePlayers.remove(removedPlayerId);
        
        for (int i = 0; i < 2 * numberOfPlayers; i++) {
            rounds.add(createRound());
            var currentSphinx = sphinxElector.selectCandidate(numberOfRoundsInTurn);
            assertNotEquals(removedPlayerId, Objects.requireNonNull(currentSphinx).getId());
        }
    }
    
    @Test
    void testRemovingCandidate_InPreviousRound() {
        List<Round> rounds = new ArrayList<>();
        final int numberOfPlayers = 10;
        Map<String, Player> activePlayers = createActivePlayers(numberOfPlayers);
        SphinxElector sphinxElector = new SphinxElector(rounds, activePlayers);
        
        final int numberOfRoundsInTurn = 2;
        
        final Round round1 = createRound();
        rounds.add(round1);
        // loads all players from activePlayers
        var sphinxForRound1 = sphinxElector.selectCandidate(numberOfRoundsInTurn);
        round1.setSphinx(sphinxForRound1);
        final String removedPlayerId = Objects.requireNonNull(sphinxForRound1).getId();
        sphinxElector.removeCandidate(removedPlayerId);
        activePlayers.remove(removedPlayerId);
        
        for (int i = 0; i < 2 * numberOfPlayers; i++) {
            rounds.add(createRound());
            var currentSphinx = sphinxElector.selectCandidate(numberOfRoundsInTurn);
            assertNotEquals(removedPlayerId, Objects.requireNonNull(currentSphinx).getId());
        }
    }
    
    private Map<String, Player> createActivePlayers(int numberOfPlayers) {
        return IntStream.range(0, numberOfPlayers)
                .mapToObj(operand -> createPlayer())
                .collect(Collectors.toMap(Player::getId, Function.identity()));
    }
}