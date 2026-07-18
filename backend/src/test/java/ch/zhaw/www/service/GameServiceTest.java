package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.utils.GameTransaction;
import ch.zhaw.www.utils.RandomProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static ch.zhaw.www.TestHelper.*;
import static ch.zhaw.www.TimeHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class GameServiceTest {
    
    private static final String GAME_ID = "GAME ID";
    private static final String UNKNOWN_PLAYER_ID = "Unknown Player";
    private static final Duration ROUND_DURATION = DEFAULT_PROPOSITION_DURATION;
    
    @Autowired
    private GameService gameService;
    @MockitoBean
    private EntityService entityService;
    @MockitoBean
    private RandomProvider randomProvider;
    @MockitoBean
    private LobbyNotifier lobbyNotifier;

    @AfterEach
    void tearDown() {
        disableFixedClocked();
    }
    
    @Test
    void testAddGameSavesItToRepository() {
        when(randomProvider.getRoomCode()).thenReturn("WXYZ");
        var game = gameService.createGame("Host");
        verify(entityService).saveNewGame(game);
        assertNotNull(game.getId());
        assertTrue(game.isHost(game.getHostId()));
    }

    @Test
    void testCreateGameRetriesOnRoomCodeCollision() {
        when(randomProvider.getRoomCode()).thenReturn("TAKN", "TAKN", "FREE");
        // First two attempts collide with an existing game, the third succeeds.
        doThrow(new GameError.ExistAlready())
                .doThrow(new GameError.ExistAlready())
                .doNothing()
                .when(entityService).saveNewGame(any(Game.class));

        var game = gameService.createGame("Host");

        assertEquals("FREE", game.getId());
        verify(randomProvider, times(3)).getRoomCode();
    }

    @Test
    void testCreateGameThrowsWhenNoFreeRoomCodeFound() {
        when(randomProvider.getRoomCode()).thenReturn("TAKN");
        doThrow(new GameError.ExistAlready()).when(entityService).saveNewGame(any(Game.class));

        assertThrows(GameError.ExistAlready.class, () -> gameService.createGame("Host"));
    }

    @Test
    void testGetGameReadsFromRepository_Found() {
        var expectedGame = mockGameInRepository();
        var actualGame = gameService.getGame(expectedGame.getId());
        assertEquals(expectedGame, actualGame);
    }
    
    @Test
    void testGetGameReadsFromRepository_NotFound() {
        var gameId = "jibberish";
        mockGameNotFoundInRepository(gameId);
        assertThrows(GameError.NotFoundException.class, () -> gameService.getGame(gameId));
    }
    
    @Test
    void testGetRoundOpenForPropositions_IllegalState() {
        var game = mockGameInRepository();
        
        Round round = createRound();
        game.addRound(round);
        List<Player> players = IntStream.range(0, 4).mapToObj(i -> registerPlayer(game))
                .peek(player -> assertThrows(RoundError.IllegalStateException.class,
                        () -> gameService.getRoundOpenForPropositions(GAME_ID, player.getId())))
                .toList();
        
        enableFixedClocked();
        round.setSphinx(getRandomPlayer(game));
        players.forEach(game::moveToActivePlayers);
        
        final String anyPlayerId = players.get(0).getId();
        assertEquals(round, gameService.getRoundOpenForPropositions(GAME_ID, anyPlayerId));
        
        offsetFixedClockBy(ROUND_DURATION);
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRoundOpenForPropositions(GAME_ID, anyPlayerId));
    }
    
    @Test
    void testGetRoundOpenForPropositions_UnknownPlayer() {
        var game = mockGameInRepository();
        Round round = createRound();
        game.addRound(round);
        IntStream.range(0, 4).forEach(i -> registerPlayer(game));
        round.setSphinx(getRandomPlayer(game));
        
        assertThrows(PlayerError.NotFoundException.class, () -> gameService.getRoundOpenForPropositions(GAME_ID, UNKNOWN_PLAYER_ID));
    }
    
    @Test
    void testGetRoundOpenForPropositions_GameNotFound() {
        mockGameNotFoundInRepository(GAME_ID);
        
        assertThrows(GameError.NotFoundException.class, () -> gameService.getRoundOpenForPropositions(GAME_ID, UNKNOWN_PLAYER_ID));
    }
    
    @Test
    void testGetRoundOpenForPropositions_ValidRound() {
        var game = mockGameInRepository();
        Round round = createRound();
        game.addRound(round);
        List<Player> players = IntStream.range(0, 4).mapToObj(i -> registerPlayer(game))
                .peek(game::moveToActivePlayers)
                .toList();
        round.setSphinx(getRandomPlayer(game));
        
        players.forEach(player -> assertEquals(round, gameService.getRoundOpenForPropositions(GAME_ID, player.getId())));
    }
    
    @Test
    void testEnterGame_GameNotFound() {
        mockGameNotFoundInRepository(GAME_ID);
        
        assertThrows(GameError.NotFoundException.class, () -> gameService.enterRound(GAME_ID, UNKNOWN_PLAYER_ID));
    }
    
    @Test
    void testEnterGame_PlayerNotFound() {
        var game = mockGameInRepository();
        game.registerPlayer(createPlayer());
        game.registerPlayer(createPlayer());
        assertThrows(PlayerError.NotFoundException.class, () -> gameService.enterRound(GAME_ID, UNKNOWN_PLAYER_ID));
    }
    
    @Test
    void testEnterGame_NoValidRound() {
        var game = mockGameInRepository();
        game.registerPlayer(createPlayer());
        final Player playerEnteringRound = createPlayer();
        game.registerPlayer(playerEnteringRound);
        
        assertFalse(game.hasActivePlayer(playerEnteringRound.getId()));
        assertNull(game.getCurrentRound());
        gameService.enterRound(GAME_ID, playerEnteringRound.getId());
        assertNotNull(game.getCurrentRound());
        assertTrue(game.hasActivePlayer(playerEnteringRound.getId()));
    }
    
    @Test
    void testEnterGame_WaitingForPlayers() {
        var game = mockGameInRepository();
        final Player player1 = createPlayer();
        game.registerPlayer(player1);
        
        gameService.enterRound(GAME_ID, player1.getId());
        Objects.requireNonNull(game.getCurrentRound()).setSphinx(player1);
        
        assertTrue(game.canRoundBeEntered());
        final Player player2 = createPlayer();
        game.registerPlayer(player2);
        
        assertFalse(game.hasActivePlayer(player2.getId()));
        gameService.enterRound(GAME_ID, player2.getId());
        assertTrue(game.hasActivePlayer(player2.getId()));
    }
    
    @Test
    void testEnterGame_WaitingForPropositions() {
        var game = mockGameInRepository();
        Player sphinx = createPlayer();
        Player player = createPlayer();
        Player otherPlayer = createPlayer();
        Player anotherPlayer = createPlayer();
        Player playerEnteringLater = createPlayer();
        List.of(sphinx, player, otherPlayer, anotherPlayer).forEach(p -> {
            game.registerPlayer(p);
            gameService.enterRound(GAME_ID, p.getId());
        });
        game.registerPlayer(playerEnteringLater);
        Objects.requireNonNull(game.getCurrentRound()).setSphinx(sphinx);
        
        assertTrue(game.canAcceptPropositionsForCurrentRound());
        
        assertFalse(game.hasActivePlayer(playerEnteringLater.getId()));
        gameService.enterRound(GAME_ID, playerEnteringLater.getId());
        assertTrue(game.hasActivePlayer(playerEnteringLater.getId()));
    }
    
    @Test
    void testEnterGame_WaitingForSelections() {
        var game = mockGameInRepository();
        Player sphinx = createPlayer();
        Player player = createPlayer();
        Player otherPlayer = createPlayer();
        Player anotherPlayer = createPlayer();
        Player cannotEnterCurrentlyPlayer = createPlayer();
        var playersInCurrentRound = List.of(sphinx, player, otherPlayer, anotherPlayer);
        playersInCurrentRound.forEach(p -> {
            game.registerPlayer(p);
            gameService.enterRound(GAME_ID, p.getId());
        });
        
        //make round ready for selections
        Objects.requireNonNull(game.getCurrentRound()).setSphinx(sphinx);
        playersInCurrentRound.forEach(p -> game.getCurrentRound().addProposition(createProposition(p.getId(), "Cereal")));
        
        assertFalse(game.canAcceptPropositionsForCurrentRound());
        
        game.registerPlayer(cannotEnterCurrentlyPlayer);
        assertFalse(game.hasActivePlayer(cannotEnterCurrentlyPlayer.getId()));
        assertThrows(RoundError.IllegalOperationException.class, () -> gameService.enterRound(GAME_ID, cannotEnterCurrentlyPlayer.getId()));
        assertFalse(game.hasActivePlayer(cannotEnterCurrentlyPlayer.getId()));
    }
    
    @Test
    void enterGameWithExistingPlayerOfSameName() {
        Game game = mockGameInRepository();
        when(randomProvider.getPostfix()).thenReturn(1982);
        
        gameService.enterGame(game.getId(), "Nora");
        gameService.enterGame(game.getId(), "Nora");
        
        var allPlayersInGame = game.getAllPlayers().count();
        assertEquals(2, allPlayersInGame);
        
        List<String> waitingListNames = game.getAllPlayers().map(Player::getName).sorted().toList();
        assertEquals("Nora", waitingListNames.get(0));
        assertEquals("Nora1982", waitingListNames.get(1));
    }
    
    @Test
    void leaveGame() {
        Game game = mockGameInRepository();
        addActivePlayer(game);
        registerPlayer(game);
        List<String> playerIDs = game.getAllPlayers().map(Player::getId).toList();
        
        gameService.leaveGame(game.getId(), playerIDs.get(0));
        assertEquals(1, game.getAllPlayers().count());
        gameService.leaveGame(game.getId(), playerIDs.get(1));
        assertEquals(0, game.getAllPlayers().count());
        assertThrows(PlayerError.NotFoundException.class, () -> gameService.leaveGame(game.getId(), playerIDs.get(0)));
        assertThrows(PlayerError.NotFoundException.class, () -> gameService.leaveGame(game.getId(), playerIDs.get(1)));
    }
    
    @Test
    void createNewRound_AtCapacity() {
        var game = mockGameInRepository();
        int nrOfPlayer = MAX_NUMBER_OF_PLAYERS + MAX_NUMBER_OF_PLAYERS / 2;
        IntStream.range(0, nrOfPlayer).forEach(value -> registerPlayer(game));
        
        String firstPlayer = getRandomPlayer(game).getId();
        gameService.enterRound(game.getId(), firstPlayer);
        var activePlayers = game.getAllPlayers().filter(player -> game.hasActivePlayer(player.getId())).count();
        assertEquals(1, activePlayers);
        assertEquals(nrOfPlayer - 1, game.getAllPlayers().count() - activePlayers);
        assertNull(Objects.requireNonNull(game.getCurrentRound()).getSphinx());
        
        game.getAllPlayers()
                .filter(player -> !player.getId().equals(firstPlayer))
                .forEach(player -> {
                    try {
                        gameService.enterRound(game.getId(), player.getId());
                    } catch (Exception ignored) {
                    }
                });
        
        activePlayers = game.getAllPlayers().filter(player -> game.hasActivePlayer(player.getId())).count();
        assertEquals(MAX_NUMBER_OF_PLAYERS, activePlayers);
        assertEquals(MAX_NUMBER_OF_PLAYERS / 2, game.getAllPlayers().count() - activePlayers);
        assertNotNull(Objects.requireNonNull(game.getCurrentRound()).getSphinx());
    }
    
    @Test
    void createNewRound_NotAtCapacity() {
        var game = mockGameInRepository();
        IntStream.range(0, MAX_NUMBER_OF_PLAYERS - 1)
                .mapToObj(i -> registerPlayer(game))
                .forEach(player -> gameService.enterRound(game.getId(), player.getId()));
        var activePlayers = game.getAllPlayers().filter(player -> game.hasActivePlayer(player.getId())).count();
        var registeredPlayers = game.getAllPlayers().count() - activePlayers;
        assertEquals(MAX_NUMBER_OF_PLAYERS - 1, activePlayers);
        assertEquals(0, registeredPlayers);
        
        assertNotNull(Objects.requireNonNull(game.getCurrentRound()).getSphinx());
    }
    
    @Test
    void testStartGameSetsStartedWhenHostAndEnoughPlayers() {
        var game = mockLobbyGameInRepository(MIN_NUMBER_OF_PLAYERS);
        gameService.startGame(GAME_ID, game.getHostId());
        assertTrue(game.isStarted());
        verify(lobbyNotifier).notifyLobbyChanged(game);
    }

    @Test
    void testStartGameThrowsWhenNotHost() {
        var game = mockLobbyGameInRepository(MIN_NUMBER_OF_PLAYERS);
        assertThrows(GameError.NotHostException.class, () -> gameService.startGame(GAME_ID, "not-the-host"));
        assertFalse(game.isStarted());
    }

    @Test
    void testStartGameThrowsWhenNotEnoughPlayers() {
        var game = mockLobbyGameInRepository(MIN_NUMBER_OF_PLAYERS - 1);
        assertThrows(GameError.NotEnoughPlayersException.class, () -> gameService.startGame(GAME_ID, game.getHostId()));
        assertFalse(game.isStarted());
    }

    @Test
    void testReassignHostIfAbsentReassignsWhenHostGoneBeyondGrace() {
        enableFixedClocked();
        var game = mockLobbyGameInRepository(MIN_NUMBER_OF_PLAYERS);
        String originalHost = game.getHostId();
        game.markPlayerDisconnected(originalHost);
        offsetFixedClockBy(Duration.ofSeconds(11));
        when(randomProvider.getRandomIndex(anyInt())).thenReturn(0);

        gameService.reassignHostIfAbsent(GAME_ID);

        assertNotEquals(originalHost, game.getHostId());
        verify(lobbyNotifier).notifyLobbyChanged(game);
    }

    @Test
    void testReassignHostIfAbsentKeepsHostWithinGrace() {
        enableFixedClocked();
        var game = mockLobbyGameInRepository(MIN_NUMBER_OF_PLAYERS);
        String originalHost = game.getHostId();
        game.markPlayerDisconnected(originalHost);
        offsetFixedClockBy(Duration.ofSeconds(5)); // still within the grace period

        gameService.reassignHostIfAbsent(GAME_ID);

        assertEquals(originalHost, game.getHostId());
        verify(lobbyNotifier, never()).notifyLobbyChanged(any());
    }

    @Test
    void testReassignHostIfAbsentNoopForStartedGame() {
        var game = mockLobbyGameInRepository(MIN_NUMBER_OF_PLAYERS);
        game.markStarted();
        String originalHost = game.getHostId();
        game.markPlayerDisconnected(originalHost); // even a gone host is ignored once started

        gameService.reassignHostIfAbsent(GAME_ID);

        assertEquals(originalHost, game.getHostId());
        verify(lobbyNotifier, never()).notifyLobbyChanged(any());
    }

    private Game mockLobbyGameInRepository(int playerCount) {
        var game = createGame(GAME_ID);
        var players = IntStream.range(0, playerCount).mapToObj(i -> registerPlayer(game)).toList();
        game.setHostId(players.get(0).getId());
        when(entityService.editGame(eq(GAME_ID), any())).thenAnswer(invocationOnMock -> {
            var lambda = invocationOnMock.getArgument(1, GameTransaction.class);
            //noinspection unchecked
            return lambda.transactionalChange(game);
        });
        when(entityService.getGame(GAME_ID)).thenReturn(game);
        return game;
    }

    private Game mockGameInRepository() {
        var game = createGame(GAME_ID);
        game.markStarted(); // these tests exercise the running-game/round flow, past the lobby

        when(entityService.editGame(eq(game.getId()), any())).thenAnswer(invocationOnMock -> {
            var lambda = invocationOnMock.getArgument(1, GameTransaction.class);
            //noinspection unchecked
            return lambda.transactionalChange(game);
        });
        when(entityService.getGame(game.getId())).thenReturn(game);
        
        return game;
    }
    
    private void mockGameNotFoundInRepository(String gameId) {
        doThrow(GameError.NotFoundException.class)
                .when(entityService).editGame(eq(gameId), any());
        
        doThrow(GameError.NotFoundException.class)
                .when(entityService).getGame(gameId);
    }
    
}