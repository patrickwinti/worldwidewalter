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
import static org.mockito.Mockito.*;

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
        assertEquals(3, round.getPropositions().get(0).getPlayerIds().size());
        
        assertEquals(players.get(0).getId(), round.getPropositions().get(0).getPlayerIds().get(0));
        assertEquals(players.get(1).getId(), round.getPropositions().get(0).getPlayerIds().get(1));
        assertEquals(players.get(2).getId(), round.getPropositions().get(0).getPlayerIds().get(2));
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
    
    @Test
    void testPropositionsRequestedTooEarly() {
        var game = mockRoundInRepository();
        var round = Objects.requireNonNull(game.getCurrentRound());
        final Player sphinx = createPlayer("Sphinx");
        var allPlayers = List.of(createPlayer("Buffy"), createPlayer("Angel"), createPlayer("Spike"), sphinx);
        var propositions = List.of("Holy Water", "Scowl", "Hydrogen Peroxide", "Beer");
        when(entityService.isPlayerActiveInRound(eq(round.getId()), any())).thenReturn(true);
        
        allPlayers.forEach(player -> {
            assertThrows(RoundError.IllegalStateException.class, () -> roundService.getRoundWithAllPropositions(round.getId(), sphinx.getId()));
            game.addPlayerToWaitingRoom(player);
            assertThrows(RoundError.IllegalStateException.class, () -> roundService.getRoundWithAllPropositions(round.getId(), sphinx.getId()));
            game.moveToActivePlayers(player);
        });
        for (int i = 0; i < allPlayers.size(); i++) {
            final var player = allPlayers.get(i).getId();
            assertThrows(RoundError.IllegalStateException.class, () -> roundService.getRoundWithAllPropositions(round.getId(), player));
            round.addProposition(createProposition(player, propositions.get(i)));
        }
        var result = roundService.getRoundWithAllPropositions(round.getId(), sphinx.getId());
        assertEquals(round, result);
    }
    
    @Test
    void testPropositionsRequested_GameNotFound() {
        var roundId = "round-1";
        var playerId = "Chuck Norris does not need an id";
        when(entityService.isPlayerActiveInRound(roundId, playerId)).thenReturn(true);
        doThrow(RoundError.NotFoundException.class).when(entityService).getGameForRound(roundId);
        assertThrows(RoundError.NotFoundException.class, () -> roundService.getRoundWithAllPropositions(roundId, playerId));
    }
    
    @Test
    void testPropositionsRequested_NoRound() {
        var roundId = "round-1";
        var playerId = "Watermelon";
        when(entityService.isPlayerActiveInRound(roundId, playerId)).thenReturn(true);
        var game = createGame();
        when(entityService.getGameForRound(roundId)).thenReturn(game);
        assertThrows(RoundError.IllegalStateException.class, () -> roundService.getRoundWithAllPropositions(roundId, playerId));
    }
    
    @Test
    void testPropositionsRequested_WrongRound() {
        var roundId = "round-1";
        var playerId = "Faberge";
        when(entityService.isPlayerActiveInRound(roundId, playerId)).thenReturn(true);
        var game = mock(Game.class);
        when(game.getCurrentRound()).thenReturn(createRound());
        when(entityService.getGameForRound(roundId)).thenReturn(game);
        assertThrows(RoundError.IllegalStateException.class, () -> roundService.getRoundWithAllPropositions(roundId, playerId));
    }
    
    @Test
    void testPropositionsRequested_PlayerNotActive() {
        var roundId = "round-1";
        var playerId = "Lorry";
        when(entityService.isPlayerActiveInRound(roundId, playerId)).thenReturn(false);
        assertThrows(PlayerError.NotFoundException.class, () -> roundService.getRoundWithAllPropositions(roundId, playerId));
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
        when(entityService.getGameForRound(round.getId())).thenReturn(game);
        return game;
    }
    
}