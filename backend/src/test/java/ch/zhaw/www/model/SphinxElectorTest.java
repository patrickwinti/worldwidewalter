package ch.zhaw.www.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, Player> activePlayers = Map.of("1", createPlayer("Player1"), "2", createPlayer("Player2"), "3", currentSphinx);
        
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
        Map<String, Player> activePlayers = Map.of("1", createPlayer("Player1"), "2", createPlayer("Player2"), "3", currentSphinx);
        
        SphinxElector sphinxElector = new SphinxElector(rounds, activePlayers);
        assertSame(currentSphinx, sphinxElector.selectCandidate(2));
    }
    
    @Test
    void testNewRound_SelectNewSphinx() {
        List<Round> rounds = new ArrayList<>();
        Map<String, Player> activePlayers = Map.of("1", createPlayer("Player1"), "2", createPlayer("Player2"), "3", createPlayer("Player3"));
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
    
}