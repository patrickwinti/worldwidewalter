package ch.zhaw.www;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Prompt;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.service.GameEntityService;
import ch.zhaw.www.service.GameError;
import ch.zhaw.www.service.GameService;
import ch.zhaw.www.service.RoundError;
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
import java.util.UUID;

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
    
    private static Round getRound() {
        return new Round("1", new Prompt("WALTER!", 1), ROUND_DURATION, 1);
    }
    
    @Test
    void testGetRound_IllegalState() {
        var game = mockGameInRepository();
        
        Round round = getRound();
        game.newRound(round);
        
        Player player1 = addPlayer(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, player1.getId()));
        addPlayer(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, player1.getId()));
        addPlayer(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, player1.getId()));
        addPlayer(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, player1.getId()));
        
        round.setSphinx(game.getActivePlayers().values().iterator().next());
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, player1.getId()));
        
        InstantWrapper.clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        round.openForPropositionSubmission();
        assertEquals(round, gameService.getRound(GAME_ID, player1.getId()));
        
        InstantWrapper.clock = Clock.offset(InstantWrapper.clock, Duration.of(ROUND_DURATION, ChronoUnit.MINUTES));
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, player1.getId()));
    }
    
    @Test
    void testGetRound_UnknownPlayer() {
        var game = mockGameInRepository();
        Round round = getRound();
        game.newRound(round);
        addPlayer(game);
        addPlayer(game);
        addPlayer(game);
        addPlayer(game);
        round.setSphinx(game.getActivePlayers().values().iterator().next());
        round.openForPropositionSubmission();
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, UNKNOWN_PLAYER_ID));
    }
    
    @Test
    void testGetRound_ValidRound() {
        var game = mockGameInRepository();
        Round round = getRound();
        game.newRound(round);
        Player player1 = addPlayer(game);
        Player player2 = addPlayer(game);
        Player player3 = addPlayer(game);
        Player player4 = addPlayer(game);
        round.setSphinx(game.getActivePlayers().values().iterator().next());
        round.openForPropositionSubmission();
        
        assertEquals(round, gameService.getRound(GAME_ID, player1.getId()));
        assertEquals(round, gameService.getRound(GAME_ID, player2.getId()));
        assertEquals(round, gameService.getRound(GAME_ID, player3.getId()));
        assertEquals(round, gameService.getRound(GAME_ID, player4.getId()));
    }
    
    @Test
    void testGetRound_GameNotFound() {
        mockGameNotFoundInRepository(GAME_ID);
        
        assertThrows(GameError.NotFoundException.class, () -> gameService.getRound(GAME_ID, UNKNOWN_PLAYER_ID));
    }
    
    private Player addPlayer(Game game) {
        Player player = new Player(UUID.randomUUID().toString());
        game.getActivePlayers().put(player.getId(), player);
        return player;
    }
    
    private Game mockGameInRepository() {
        var game = new Game(GAME_ID);
        
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