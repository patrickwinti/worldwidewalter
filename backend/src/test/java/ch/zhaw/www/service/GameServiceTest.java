package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Prompt;
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

    private static Player getRandomPlayer(Game game) {
        return game.getWaitingRoom().values().iterator().next();
    }

    @Test
    void testGetRound_IllegalState() {
        var game = mockGameInRepository();

        Round round = getRound();
        game.addRound(round);

        Player player1 = addWaitingRoomPlayer(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, player1.getId()));
        Player player2 = addWaitingRoomPlayer(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, player1.getId()));
        Player player3 = addWaitingRoomPlayer(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, player1.getId()));
        Player player4 = addWaitingRoomPlayer(game);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, player1.getId()));

        InstantWrapper.clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        round.setSphinx(getRandomPlayer(game));
        addActivePlayer(game, player1);
        addActivePlayer(game, player2);
        addActivePlayer(game, player3);
        addActivePlayer(game, player4);
        assertEquals(round, gameService.getRound(GAME_ID, player1.getId()));

        InstantWrapper.clock = Clock.offset(InstantWrapper.clock, Duration.of(ROUND_DURATION, ChronoUnit.MINUTES));

        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, player1.getId()));
    }

    @Test
    void testGetRound_UnknownPlayer() {
        var game = mockGameInRepository();
        Round round = getRound();
        game.addRound(round);
        addWaitingRoomPlayer(game);
        addWaitingRoomPlayer(game);
        addWaitingRoomPlayer(game);
        addWaitingRoomPlayer(game);
        round.setSphinx(getRandomPlayer(game));

        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRound(GAME_ID, UNKNOWN_PLAYER_ID));
    }

    @Test
    void testGetRound_GameNotFound() {
        mockGameNotFoundInRepository(GAME_ID);

        assertThrows(GameError.NotFoundException.class, () -> gameService.getRound(GAME_ID, UNKNOWN_PLAYER_ID));
    }

    @Test
    void testGetRound_ValidRound() {
        var game = mockGameInRepository();
        Round round = getRound();
        game.addRound(round);
        Player player1 = addWaitingRoomPlayer(game);
        Player player2 = addWaitingRoomPlayer(game);
        Player player3 = addWaitingRoomPlayer(game);
        Player player4 = addWaitingRoomPlayer(game);
        round.setSphinx(getRandomPlayer(game));
        addActivePlayer(game, player1);
        addActivePlayer(game, player2);
        addActivePlayer(game, player3);
        addActivePlayer(game, player4);

        assertEquals(round, gameService.getRound(GAME_ID, player1.getId()));
        assertEquals(round, gameService.getRound(GAME_ID, player2.getId()));
        assertEquals(round, gameService.getRound(GAME_ID, player3.getId()));
        assertEquals(round, gameService.getRound(GAME_ID, player4.getId()));
    }

    private Player addWaitingRoomPlayer(Game game) {
        Player player = new Player(UUID.randomUUID().toString(), "Luna");
        game.getWaitingRoom().put(player.getId(), player);
        return player;
    }

    private Game mockGameInRepository() {
        var game = new Game(GAME_ID);

        doNothing().when(gameEntityService).editGame(eq(game.getId()), any());
        when(gameEntityService.getGame(game.getId())).thenReturn(game);

        return game;
    }

    private void addActivePlayer(Game game, Player player) {
        game.getActivePlayers().put(player.getId(), player);
    }

    private void mockGameNotFoundInRepository(String gameId) {
        doThrow(GameError.NotFoundException.class)
                .when(gameEntityService).editGame(eq(gameId), any());

        doThrow(GameError.NotFoundException.class)
                .when(gameEntityService).getGame(gameId);
    }

    @Test
    void enterGame() {
        // 1. mock a game in the repository
        var game = mockGameInRepository();
        // 2. call the method under test
        var player1 = gameService.enterGame(game.getId(), "Nora");
        var player2 = gameService.enterGame(game.getId(), "Nora");
        // 3. add player1 to the game in activePlayers
        game.getActivePlayers().put(player1.getId(), player1);
        // 4. remove player1 from the game in waitingRoom
        game.getWaitingRoom().remove(player1.getId());
        // 5. add another player with the same name
        var player3 = gameService.enterGame(game.getId(), "Nora");
        // 6. verify that 3 players were added to the game
        assertEquals(2, game.getWaitingRoom().size());
        assertEquals(1, game.getActivePlayers().size());
        assertEquals(player1, game.getActivePlayers().get(player1.getId()));
        assertEquals(player2, game.getWaitingRoom().get(player2.getId()));
        assertEquals(player3, game.getWaitingRoom().get(player3.getId()));
        // 7. verify that player 1 is in activePlayers and player 2 and 3 are in waitingRoom
        assertFalse(game.getWaitingRoom().containsKey(player1.getId()));
        assertTrue(game.getActivePlayers().containsKey(player1.getId()));
        assertTrue(game.getWaitingRoom().containsKey(player2.getId()));
        assertTrue(game.getWaitingRoom().containsKey(player3.getId()));

        assertEquals("Nora", game.getActivePlayers().get(player1.getId()).getName());
        assertEquals("Nora 😏", game.getWaitingRoom().get(player2.getId()).getName());
        assertEquals("Nora 🦅", game.getWaitingRoom().get(player3.getId()).getName());
    }
}