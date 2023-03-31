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

import static ch.zhaw.www.model.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class GameServiceTest {
    
    private static final String GAME_ID = "GAME ID";
    private static final String UNKNOWN_PLAYER_ID = "Unknown Player";
    private static final int ROUND_DURATION = 4;
    
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
        
        Round round = getRound();
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
        markActivePlayer(game, player1);
        markActivePlayer(game, player2);
        markActivePlayer(game, player3);
        markActivePlayer(game, player4);
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        
        InstantWrapper.clock = Clock.offset(InstantWrapper.clock, Duration.of(ROUND_DURATION, ChronoUnit.MINUTES));
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
    }
    
    @Test
    void testGetRound_UnknownPlayer() {
        var game = mockGameInRepository();
        Round round = getRound();
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
        Round round = getRound();
        game.addRound(round);
        Player player1 = addToWaitingRoom(game);
        Player player2 = addToWaitingRoom(game);
        Player player3 = addToWaitingRoom(game);
        Player player4 = addToWaitingRoom(game);
        round.setSphinx(getRandomPlayer(game));
        markActivePlayer(game, player1);
        markActivePlayer(game, player2);
        markActivePlayer(game, player3);
        markActivePlayer(game, player4);
        
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player1.getId()));
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player2.getId()));
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player3.getId()));
        assertEquals(round, gameService.getCurrentRoundInGame(GAME_ID, player4.getId()));
    }
    
    private Game mockGameInRepository() {
        var game = getGame(GAME_ID);
        
        doNothing().when(gameEntityService).editGame(eq(game.getId()), any());
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