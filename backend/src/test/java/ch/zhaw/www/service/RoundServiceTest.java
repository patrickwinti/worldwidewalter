package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.zhaw.www.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
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
        when(entityService.isPlayerActiveInRound(any(), any())).thenReturn(true);
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
    
    @Test
    void testSelectSphinx() {
        int roundsInTurn = 3;
        Game game = createGame(roundsInTurn);
        var alice = createPlayer("Alice");
        var bob = createPlayer("Bob");
        var charlie = createPlayer("Charlie");
        var dave = createPlayer("Dave");
        game.setSphinxCandidates(Stream.of(alice, bob, charlie, dave)
                .peek(game::addPlayerToWaitingRoom)
                .peek(game::moveToActivePlayers)
                .map(player -> Map.entry(player, player == bob ? roundsInTurn - 1 : roundsInTurn))
                .collect(Collectors.toSet()));
        
        game.addRound(createRound());
        roundService.selectSphinx(game);
        Player selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        Map<Player, Integer> candidates = game.getSphinxCandidates().stream()
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        assertEquals("Bob", Objects.requireNonNull(selected).getName());
        assertEquals(roundsInTurn, candidates.get(alice));
        assertEquals(1, candidates.get(bob));
        assertEquals(roundsInTurn, candidates.get(charlie));
        assertEquals(roundsInTurn, candidates.get(dave));
        
        game.addRound(createRound());
        roundService.selectSphinx(game);
        selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        candidates = game.getSphinxCandidates().stream()
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        assertEquals("Bob", Objects.requireNonNull(selected).getName());
        assertEquals(roundsInTurn, candidates.get(alice));
        assertFalse(candidates.containsKey(bob));
        assertEquals(roundsInTurn, candidates.get(charlie));
        assertEquals(roundsInTurn, candidates.get(dave));
        
        game.addRound(createRound());
        roundService.selectSphinx(game);
        selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        candidates = game.getSphinxCandidates().stream()
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        assertNotEquals("Bob", Objects.requireNonNull(selected).getName());
        assertTrue(candidates.containsValue(roundsInTurn - 1));
        assertFalse(candidates.containsKey(bob));
    }
    
    @Test
    void testSelectSphinx_notActiveAnymore() {
        int roundsInTurn = 3;
        Game game = createGame(roundsInTurn);
        var alice = createPlayer("Alice");
        var bob = createPlayer("Bob");
        var charlie = createPlayer("Charlie");
        var dave = createPlayer("Dave");
        game.setSphinxCandidates(Stream.of(alice, bob, charlie, dave)
                .peek(game::addPlayerToWaitingRoom)
                .peek(game::moveToActivePlayers)
                .map(player -> Map.entry(player, player == bob ? roundsInTurn - 1 : roundsInTurn))
                .collect(Collectors.toSet()));
        
        game.addRound(createRound());
        roundService.selectSphinx(game);
        Player selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        Map<Player, Integer> candidates = game.getSphinxCandidates().stream()
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        assertEquals("Bob", Objects.requireNonNull(selected).getName());
        assertEquals(roundsInTurn, candidates.get(alice));
        assertEquals(1, candidates.get(bob));
        assertEquals(roundsInTurn, candidates.get(charlie));
        assertEquals(roundsInTurn, candidates.get(dave));
        
        roundService.selectSphinx(game);
        selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        candidates = game.getSphinxCandidates().stream()
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        assertEquals("Bob", Objects.requireNonNull(selected).getName());
        assertEquals(roundsInTurn, candidates.get(alice));
        assertEquals(1, candidates.get(bob));
        assertEquals(roundsInTurn, candidates.get(charlie));
        assertEquals(roundsInTurn, candidates.get(dave));
        
    }
    
    @Test
    void testSelectSphinx_sphinxSelected() {
        int roundsInTurn = 8;
        Game game = createGame(roundsInTurn);
        var alice = createPlayer("Alice");
        var bob = createPlayer("Bob");
        var charlie = createPlayer("Charlie");
        var dave = createPlayer("Dave");
        
        game.setSphinxCandidates(Stream.of(alice, bob, charlie, dave)
                .peek(game::addPlayerToWaitingRoom)
                .map(player -> Map.entry(player, player == bob ? roundsInTurn - 1 : roundsInTurn))
                .collect(Collectors.toSet()));
        
        game.addRound(createRound());
        roundService.selectSphinx(game);
        Player selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        assertNull(selected);
        
        game.moveToActivePlayers(alice);
        game.moveToActivePlayers(charlie);
        
        roundService.selectSphinx(game);
        selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        assertNotNull(selected);
    }
    
    @SuppressWarnings("unchecked")
    private Game mockRoundInRepository() {
        var game = createGame(GAME_ID);
        var round = createRound();
        round.setSphinx(createPlayer());
        game.addRound(round);
        doAnswer(invocationOnMock -> {
            var lambda = invocationOnMock.getArgument(1, Consumer.class);
            lambda.accept(round);
            return null;
        }).when(entityService).editRound(eq(round.getId()), any());
        when(entityService.getRound(round.getId())).thenReturn(round);
        return game;
    }
    
}