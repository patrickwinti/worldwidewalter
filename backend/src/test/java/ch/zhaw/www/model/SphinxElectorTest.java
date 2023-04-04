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
        var currentSphinx = createPlayer("Player3");
        var round1 = createRound();
        var round2 = createRound();
        var round3 = createRound();
        round1.setSphinx(currentSphinx);
        round2.setSphinx(currentSphinx);
        List<Round> rounds = List.of(round1, round2, round3);
        Map<String, Player> activePlayers = Map.of("1", createPlayer("Player1"), "2", createPlayer("Player2"), "3", currentSphinx);
        
        SphinxElector sphinxElector = new SphinxElector(rounds, activePlayers);
        assertNotSame(currentSphinx, sphinxElector.selectCandidate(2));
    }
    
}