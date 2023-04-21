package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static ch.zhaw.www.TestHelper.*;
import static ch.zhaw.www.TimeHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class GameServiceTest {
    
    private static final String GAME_ID = "GAME ID";
    private static final String UNKNOWN_PLAYER_ID = "Unknown Player";
    private static final Duration ROUND_DURATION = Duration.of(4, ChronoUnit.MINUTES);
    
    @Autowired
    private GameService gameService;
    @MockBean
    private EntityService entityService;
    
    @AfterEach
    void tearDown() {
        disableFixedClocked();
    }
    
    @Test
    void testAddGameSavesItToRepository() {
        var game = gameService.createGame();
        verify(entityService).saveNewGame(game);
        assertNotNull(game.getId());
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
    void testGetCurrentRound_IllegalState() {
        var game = mockGameInRepository();
        
        Round round = createRound();
        game.addRound(round);
        
        Player player1 = addWaitingRoomPlayer(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        Player player2 = addWaitingRoomPlayer(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        Player player3 = addWaitingRoomPlayer(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        Player player4 = addWaitingRoomPlayer(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        
        enableFixedClocked();
        round.setSphinx(getRandomPlayer(game));
        game.moveToActivePlayers(player1);
        game.moveToActivePlayers(player2);
        game.moveToActivePlayers(player3);
        game.moveToActivePlayers(player4);
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        
        offsetFixedClockBy(ROUND_DURATION);
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
    }
    
    @Test
    void testGetCurrentRound_UnknownPlayer() {
        var game = mockGameInRepository();
        Round round = createRound();
        game.addRound(round);
        addWaitingRoomPlayer(game);
        addWaitingRoomPlayer(game);
        addWaitingRoomPlayer(game);
        addWaitingRoomPlayer(game);
        round.setSphinx(getRandomPlayer(game));
        
        assertThrows(PlayerError.NotFoundException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, UNKNOWN_PLAYER_ID));
    }
    
    @Test
    void testGetCurrentRound_GameNotFound() {
        mockGameNotFoundInRepository(GAME_ID);
        
        assertThrows(GameError.NotFoundException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, UNKNOWN_PLAYER_ID));
    }
    
    @Test
    void testGetCurrentRound_ValidRound() {
        var game = mockGameInRepository();
        Round round = createRound();
        game.addRound(round);
        Player player1 = addWaitingRoomPlayer(game);
        Player player2 = addWaitingRoomPlayer(game);
        Player player3 = addWaitingRoomPlayer(game);
        Player player4 = addWaitingRoomPlayer(game);
        round.setSphinx(getRandomPlayer(game));
        game.moveToActivePlayers(player1);
        game.moveToActivePlayers(player2);
        game.moveToActivePlayers(player3);
        game.moveToActivePlayers(player4);
        
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player2.getId()));
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player3.getId()));
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player4.getId()));
    }
    
    @Test
    void testEnterGame_GameNotFound() {
        mockGameNotFoundInRepository(GAME_ID);
        
        assertThrows(GameError.NotFoundException.class, () -> gameService.enterRound(GAME_ID, UNKNOWN_PLAYER_ID));
    }
    
    @Test
    void testEnterGame_PlayerNotFound() {
        var game = mockGameInRepository();
        game.addPlayerToWaitingRoom(createPlayer());
        game.addPlayerToWaitingRoom(createPlayer());
        assertThrows(PlayerError.NotFoundException.class, () -> gameService.enterRound(GAME_ID, UNKNOWN_PLAYER_ID));
    }
    
    @Test
    void testEnterGame_NoValidRound() {
        var game = mockGameInRepository();
        game.addPlayerToWaitingRoom(createPlayer());
        final Player playerEnteringRound = createPlayer();
        game.addPlayerToWaitingRoom(playerEnteringRound);
        
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
        game.addPlayerToWaitingRoom(player1);
        
        gameService.enterRound(GAME_ID, player1.getId());
        Objects.requireNonNull(game.getCurrentRound()).setSphinx(player1);
        
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getState());
        final Player player2 = createPlayer();
        game.addPlayerToWaitingRoom(player2);
        
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
            game.addPlayerToWaitingRoom(p);
            gameService.enterRound(GAME_ID, p.getId());
        });
        game.addPlayerToWaitingRoom(playerEnteringLater);
        Objects.requireNonNull(game.getCurrentRound()).setSphinx(sphinx);
        
        assertEquals(Game.State.WAITING_FOR_ALL_PROPOSITIONS, game.getState());
        
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
            game.addPlayerToWaitingRoom(p);
            gameService.enterRound(GAME_ID, p.getId());
        });
        
        //make round ready for selections
        Objects.requireNonNull(game.getCurrentRound()).setSphinx(sphinx);
        playersInCurrentRound.forEach(p -> game.getCurrentRound().addProposition(createProposition(p.getId(), "Cereal")));
        
        assertEquals(Game.State.WAITING_FOR_ALL_SELECTIONS, game.getState());
        
        game.addPlayerToWaitingRoom(cannotEnterCurrentlyPlayer);
        assertFalse(game.hasActivePlayer(cannotEnterCurrentlyPlayer.getId()));
        gameService.enterRound(GAME_ID, cannotEnterCurrentlyPlayer.getId());
        assertFalse(game.hasActivePlayer(cannotEnterCurrentlyPlayer.getId()));
    }
    
    @Test
    void enterGameWithExistingPlayerOfSameName() {
        Game game = mockGameInRepository();
        
        gameService.enterGame(game.getId(), "Nora");
        gameService.enterGame(game.getId(), "Nora");
        
        var allPlayersInGame = game.getAllPlayers().count();
        assertEquals(2, allPlayersInGame);
        List<String> waitingListNames = game.getAllPlayers().map(Player::getName).sorted().toList();
        assertEquals("Nora", waitingListNames.get(0));
        assertEquals("Nora1360", waitingListNames.get(1));
    }
    
    @Test
    void leaveGame() {
        Game game = mockGameInRepository();
        addActivePlayer(game);
        addWaitingRoomPlayer(game);
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
        IntStream.range(0, MAX_NUMBER_OF_PLAYERS + MAX_NUMBER_OF_PLAYERS / 2).forEach(value -> addWaitingRoomPlayer(game));
        
        gameService.enterRound(game.getId(), getRandomPlayer(game).getId());
        var activePlayers = game.getAllPlayers().filter(player -> game.hasActivePlayer(player.getId())).count();
        var waitingRoomPlayers = game.getAllPlayers().count() - activePlayers;
        assertEquals(MAX_NUMBER_OF_PLAYERS, activePlayers);
        assertEquals(MAX_NUMBER_OF_PLAYERS / 2, waitingRoomPlayers);
    }
    
    private Game mockGameInRepository() {
        var game = createGame(GAME_ID);
        
        doAnswer(invocationOnMock -> {
            var lambda = invocationOnMock.getArgument(1, Consumer.class);
            //noinspection unchecked
            lambda.accept(game);
            return null;
        }).when(entityService).editGame(eq(game.getId()), any());
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