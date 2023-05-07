package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.utils.RoundTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ch.zhaw.www.TestHelper.*;
import static ch.zhaw.www.TimeHelper.enableFixedClocked;
import static ch.zhaw.www.TimeHelper.offsetFixedClockBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
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
            game.registerPlayer(player);
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
    void testSubmitProposition_NoSphinx() {
        var game = createGame(GAME_ID);
        var round = createRound();
        game.addRound(round);
        when(entityService.editRound(eq(round.getId()), any())).thenAnswer(invocationOnMock -> {
            var lambda = invocationOnMock.getArgument(1, RoundTransaction.class);
            return lambda.transactionalChange(game, round);
        });
        when(entityService.getRound(round.getId())).thenReturn(round);
        when(entityService.getGameForRound(round.getId())).thenReturn(Pair.of(game, round));
        var players = List.of(createPlayer(), createPlayer(), createPlayer());
        when(entityService.isPlayerActiveInRound(any(), any())).thenReturn(true);
        
        assertThrows(RoundError.IllegalStateException.class, () -> roundService.submitProposition(round.getId(), players.get(0).getId(), List.of("Wasser")));
    }
    
    @Test
    void testSelectSphinx() {
        int roundsInTurn = 3;
        Game game = createGame(roundsInTurn);
        var alice = createPlayer("Alice");
        var bob = createPlayer("Bob");
        var charlie = createPlayer("Charlie");
        var dave = createPlayer("Dave");
        game.addRound(createRound());
        
        game.setSphinxCandidates(Stream.of(alice, bob, charlie, dave)
                .peek(game::registerPlayer)
                .peek(game::moveToActivePlayers)
                .map(player -> Map.entry(player, player == bob ? roundsInTurn - 1 : roundsInTurn))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
        
        roundService.selectSphinx(game);
        Player selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        Map<Player, Integer> candidates = game.getSphinxCandidates();
        assertEquals("Bob", Objects.requireNonNull(selected).getName());
        assertEquals(roundsInTurn, candidates.get(alice));
        assertEquals(1, candidates.get(bob));
        assertEquals(roundsInTurn, candidates.get(charlie));
        assertEquals(roundsInTurn, candidates.get(dave));
        
        game.addRound(createRound());
        Stream.of(alice, bob, charlie, dave).forEach(game::moveToActivePlayers);
        roundService.selectSphinx(game);
        selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        candidates = game.getSphinxCandidates();
        assertEquals("Bob", Objects.requireNonNull(selected).getName());
        assertEquals(roundsInTurn, candidates.get(alice));
        assertFalse(candidates.containsKey(bob));
        assertEquals(roundsInTurn, candidates.get(charlie));
        assertEquals(roundsInTurn, candidates.get(dave));
        
        game.addRound(createRound());
        Stream.of(alice, bob, charlie, dave).forEach(game::moveToActivePlayers);
        roundService.selectSphinx(game);
        selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        candidates = game.getSphinxCandidates();
        assertNotEquals("Bob", Objects.requireNonNull(selected).getName());
        assertTrue(candidates.containsValue(roundsInTurn - 1));
        assertFalse(candidates.containsKey(bob));
    }
    
    @Test
    void testSelectSphinx_NotEnoughPlayers() {
        int roundsInTurn = 3;
        Game game = createGame(roundsInTurn);
        var alice = createPlayer("Alice");
        var bob = createPlayer("Bob");
        game.addRound(createRound());
        
        game.setSphinxCandidates(Stream.of(alice, bob)
                .peek(game::registerPlayer)
                .peek(game::moveToActivePlayers)
                .map(player -> Map.entry(player, player == bob ? roundsInTurn - 1 : roundsInTurn))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
        
        roundService.selectSphinx(game);
        assertNull(Objects.requireNonNull(game.getCurrentRound()).getSphinx());
        var charlie = createPlayer("Charlie");
        var dave = createPlayer("Dave");
        game.registerPlayer(charlie);
        game.moveToActivePlayers(charlie);
        game.registerPlayer(dave);
        game.moveToActivePlayers(dave);
        roundService.selectSphinx(game);
        assertEquals(bob, Objects.requireNonNull(game.getCurrentRound()).getSphinx());
    }
    
    @Test
    void testSelectSphinx_notActiveAnymore() {
        int roundsInTurn = 3;
        Game game = createGame(roundsInTurn);
        var alice = createPlayer("Alice");
        var bob = createPlayer("Bob");
        var charlie = createPlayer("Charlie");
        var dave = createPlayer("Dave");
        game.addRound(createRound());
        
        game.setSphinxCandidates(Stream.of(alice, bob, charlie, dave)
                .peek(game::registerPlayer)
                .peek(game::moveToActivePlayers)
                .map(player -> Map.entry(player, player == bob ? roundsInTurn - 1 : roundsInTurn))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
        
        roundService.selectSphinx(game);
        Player selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        Map<Player, Integer> candidates = game.getSphinxCandidates();
        assertEquals("Bob", Objects.requireNonNull(selected).getName());
        assertEquals(roundsInTurn, candidates.get(alice));
        assertEquals(1, candidates.get(bob));
        assertEquals(roundsInTurn, candidates.get(charlie));
        assertEquals(roundsInTurn, candidates.get(dave));
        
        roundService.selectSphinx(game);
        selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        candidates = game.getSphinxCandidates();
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
                .peek(game::registerPlayer)
                .map(player -> Map.entry(player, player == bob ? roundsInTurn - 1 : roundsInTurn))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
        
        game.addRound(createRound());
        roundService.selectSphinx(game);
        Player selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        assertNull(selected);
        
        Stream.of(alice, bob, charlie, dave).forEach(game::moveToActivePlayers);
        
        roundService.selectSphinx(game);
        selected = Objects.requireNonNull(game.getCurrentRound()).getSphinx();
        assertNotNull(selected);
    }
    
    @Test
    void testPropositionsRequestedTooEarly() {
        var game = mockRoundInRepository();
        var round = Objects.requireNonNull(game.getCurrentRound());
        final Player sphinx = round.getSphinx();
        var allPlayers = List.of(createPlayer("Buffy"), createPlayer("Angel"), createPlayer("Spike"), sphinx);
        var propositions = List.of("Holy Water", "Scowl", "Hydrogen Peroxide", "Beer");
        when(entityService.isPlayerActiveInRound(eq(round.getId()), any())).thenReturn(true);
        
        allPlayers.forEach(player -> {
            game.registerPlayer(player);
            game.moveToActivePlayers(player);
        });
        
        for (int i = 0; i < allPlayers.size(); i++) {
            final var player = allPlayers.get(i);
            assertThrows(RoundError.IllegalStateException.class, () -> roundService.getRoundReadyForSelections(round.getId(), player.getId()));
            round.addProposition(createProposition(player.getId(), propositions.get(i)));
        }
        var result = roundService.getRoundReadyForSelections(round.getId(), sphinx.getId());
        assertEquals(round, result);
    }
    
    @Test
    void testPropositionsRequested_GameNotFound() {
        var roundId = "round-1";
        var playerId = "Chuck Norris does not need an id";
        when(entityService.isPlayerActiveInRound(roundId, playerId)).thenReturn(true);
        doThrow(RoundError.NotFoundException.class).when(entityService).getGameForRound(roundId);
        assertThrows(RoundError.NotFoundException.class, () -> roundService.getRoundReadyForSelections(roundId, playerId));
    }
    
    @Test
    void testPropositionsRequested_NoRound() {
        var roundId = "round-1";
        var playerId = "Watermelon";
        when(entityService.isPlayerActiveInRound(roundId, playerId)).thenReturn(true);
        doThrow(RoundError.NotFoundException.class).when(entityService).getGameForRound(roundId);
        assertThrows(RoundError.NotFoundException.class, () -> roundService.getRoundReadyForSelections(roundId, playerId));
    }
    
    @Test
    void testPropositionsRequested_PlayerNotActive() {
        var roundId = "round-1";
        var playerId = "Lorry";
        when(entityService.isPlayerActiveInRound(roundId, playerId)).thenReturn(false);
        assertThrows(PlayerError.NotFoundException.class, () -> roundService.getRoundReadyForSelections(roundId, playerId));
    }
    
    @Test
    void selectProposition_playerNotActive() {
        var roundId = "round-1";
        var playerId = "Lorry";
        var propositionId = "myProp";
        when(entityService.isPlayerActiveInRound(roundId, playerId)).thenReturn(false);
        assertThrows(PlayerError.NotFoundException.class, () -> roundService.selectProposition(roundId, playerId, propositionId));
    }
    
    @Test
    void selectProposition() {
        var game = mockRoundInRepository();
        var players = List.of(createPlayer(), createPlayer(), createPlayer());
        when(entityService.isPlayerActiveInRound(any(), any())).thenReturn(true);
        players.forEach(player -> {
            game.registerPlayer(player);
            game.moveToActivePlayers(player);
        });
        var round = Objects.requireNonNull(game.getCurrentRound());
        roundService.submitProposition(round.getId(), players.get(0).getId(), List.of("Wasser", "Gummi"));
        var propId = round.getPropositions().get(0).getId();
        roundService.selectProposition(round.getId(), players.get(1).getId(), propId);
        
        assertEquals(1, round.getSelections().size());
        assertTrue(round.getSelections().containsKey(players.get(1).getId()));
        assertEquals(round.getSelections().get(players.get(1).getId()), propId);
    }
    
    @Test
    void selectProposition_isSphinx() {
        var game = mockRoundInRepository();
        var players = List.of(createPlayer(), createPlayer(), createPlayer());
        when(entityService.isPlayerActiveInRound(any(), any())).thenReturn(true);
        players.forEach(player -> {
            game.registerPlayer(player);
            game.moveToActivePlayers(player);
        });
        var round = Objects.requireNonNull(game.getCurrentRound());
        var sphinxId = Objects.requireNonNull(round.getSphinx()).getId();
        roundService.submitProposition(round.getId(), players.get(0).getId(), List.of("Wasser", "Gummi"));
        var propId = round.getPropositions().get(0).getId();
        
        assertThrows(RoundError.IllegalOperationException.class, () -> roundService.selectProposition(round.getId(), sphinxId, propId));
    }
    
    @Test
    void selectProposition_notExistingProposition() {
        var game = mockRoundInRepository();
        var players = List.of(createPlayer(), createPlayer(), createPlayer());
        when(entityService.isPlayerActiveInRound(any(), any())).thenReturn(true);
        players.forEach(player -> {
            game.registerPlayer(player);
            game.moveToActivePlayers(player);
        });
        var round = Objects.requireNonNull(game.getCurrentRound());
        roundService.submitProposition(round.getId(), players.get(0).getId(), List.of("Wasser", "Gummi"));
        var propId = "notExistingPropId";
        
        assertThrows(RoundError.IllegalOperationException.class, () -> roundService.selectProposition(round.getId(), players.get(0).getId(), propId));
    }
    
    @Test
    void testGetRoundClosedForSelections_IllegalState() {
        when(entityService.isPlayerActiveInRound(any(), any())).thenReturn(true);
        var game = mockRoundInRepository();
        Round round = Objects.requireNonNull(game.getCurrentRound());
        List<Player> players = IntStream.range(0, 4).mapToObj(i -> registerPlayer(game)).peek(player -> assertThrows(RoundError.IllegalStateException.class, () -> roundService.getRoundClosedForSelections(round.getId(), player.getId()))).toList();
        
        enableFixedClocked();
        players.forEach(player -> {
            game.moveToActivePlayers(player);
            assertThrows(RoundError.IllegalStateException.class, () -> roundService.getRoundClosedForSelections(round.getId(), player.getId()));
            round.addProposition(createProposition(player.getId(), "Test"));
            round.addSelection(player.getId(), UUID.randomUUID().toString());
        });
        
        final String anyPlayerId = players.get(0).getId();
        assertEquals(round, roundService.getRoundClosedForSelections(round.getId(), anyPlayerId));
        offsetFixedClockBy(DEFAULT_PROPOSITION_DURATION.plus(DEFAULT_SUBMISSION_DURATION));
    }
    
    @Test
    void testGetRoundClosedForSelections_UnknownPlayer() {
        var game = mockRoundInRepository();
        Round round = Objects.requireNonNull(game.getCurrentRound());
        IntStream.range(0, 4).forEach(i -> registerPlayer(game));
        round.setSphinx(getRandomPlayer(game));
        
        assertThrows(PlayerError.NotFoundException.class, () -> roundService.getRoundClosedForSelections(round.getId(), "player"));
    }
    
    @Test
    void testGetRoundClosedForSelections_GameNotFound() {
        String id = "any";
        doThrow(GameError.NotFoundException.class).when(entityService).getGameForRound(id);
        when(entityService.isPlayerActiveInRound(any(), any())).thenReturn(true);
        
        assertThrows(GameError.NotFoundException.class, () -> roundService.getRoundClosedForSelections(id, "player"));
    }
    
    @Test
    void testGetRoundClosedForSelections_ValidRound() {
        when(entityService.isPlayerActiveInRound(any(), any())).thenReturn(true);
        var game = mockRoundInRepository();
        Round round = Objects.requireNonNull(game.getCurrentRound());
        List<Player> players = IntStream.range(0, 4).mapToObj(i -> registerPlayer(game)).toList();
        players.forEach(player -> {
            game.moveToActivePlayers(player);
            assertThrows(RoundError.IllegalStateException.class, () -> roundService.getRoundClosedForSelections(round.getId(), player.getId()));
            round.addProposition(createProposition(player.getId(), "Test"));
            round.addSelection(player.getId(), UUID.randomUUID().toString());
        });
        
        players.forEach(player -> assertEquals(round, roundService.getRoundClosedForSelections(round.getId(), player.getId())));
    }
    
    private Game mockRoundInRepository() {
        var game = createGame(GAME_ID);
        var round = createRound();
        var sphinx = createPlayer();
        game.addRound(round);
        game.moveToActivePlayers(sphinx);
        round.setSphinx(sphinx);
        when(entityService.editRound(eq(round.getId()), any())).thenAnswer(invocationOnMock -> {
            var lambda = invocationOnMock.getArgument(1, RoundTransaction.class);
            return lambda.transactionalChange(game, round);
        });
        when(entityService.getRound(round.getId())).thenReturn(round);
        when(entityService.getGameForRound(round.getId())).thenReturn(Pair.of(game, round));
        return game;
    }
    
}