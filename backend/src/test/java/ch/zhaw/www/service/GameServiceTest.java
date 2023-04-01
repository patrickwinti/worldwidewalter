package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.utils.InstantWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.UnaryOperator;

import static ch.zhaw.www.TestHelper.*;
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
    private GameEntityService gameEntityService;
    
    @Test
    void testAddGameSavesItToRepository() {
        var game = gameService.createGame();
        verify(gameEntityService).saveNewGame(game);
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
    void testGetRound_IllegalState() {
        var game = mockGameInRepository();
        
        Round round = createRound();
        game.addRound(round);
        
        Player player1 = addToWaitingRoom(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        Player player2 = addToWaitingRoom(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        Player player3 = addToWaitingRoom(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        Player player4 = addToWaitingRoom(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        
        InstantWrapper.clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        round.setSphinx(getRandomPlayer(game));
        game.markPlayerAsActive(player1);
        game.markPlayerAsActive(player2);
        game.markPlayerAsActive(player3);
        game.markPlayerAsActive(player4);
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        
        InstantWrapper.clock = Clock.offset(InstantWrapper.clock, ROUND_DURATION);
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
    }
    
    @Test
    void testGetRound_UnknownPlayer() {
        var game = mockGameInRepository();
        Round round = createRound();
        game.addRound(round);
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        round.setSphinx(getRandomPlayer(game));
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, UNKNOWN_PLAYER_ID));
    }
    
    @Test
    void testGetRound_GameNotFound() {
        mockGameNotFoundInRepository(GAME_ID);
        
        assertThrows(GameError.NotFoundException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, UNKNOWN_PLAYER_ID));
    }
    
    @Test
    void testGetRound_ValidRound() {
        var game = mockGameInRepository();
        Round round = createRound();
        game.addRound(round);
        Player player1 = addToWaitingRoom(game);
        Player player2 = addToWaitingRoom(game);
        Player player3 = addToWaitingRoom(game);
        Player player4 = addToWaitingRoom(game);
        round.setSphinx(getRandomPlayer(game));
        game.markPlayerAsActive(player1);
        game.markPlayerAsActive(player2);
        game.markPlayerAsActive(player3);
        game.markPlayerAsActive(player4);
        
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player2.getId()));
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player3.getId()));
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player4.getId()));
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
    
    private Game mockGameInRepository() {
        var game = createGame(GAME_ID);
        
        doAnswer(invocationOnMock -> {
            var lambda = invocationOnMock.getArgument(1, UnaryOperator.class);
            //noinspection unchecked
            lambda.apply(game);
            return null;
        }).when(gameEntityService).editGame(eq(game.getId()), any());
        when(gameEntityService.getGame(game.getId())).thenReturn(game);
        
        return game;
    }
    
    private void mockGameNotFoundInRepository(String gameId) {
        doThrow(GameError.NotFoundException.class)
                .when(gameEntityService).editGame(eq(gameId), any());
        
        doThrow(GameError.NotFoundException.class)
                .when(gameEntityService).getGame(gameId);
    }
    
}