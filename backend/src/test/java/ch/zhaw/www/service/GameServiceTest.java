package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Prompt;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.utils.InstantWrapper;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;

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
        Game game = mockGameInRepository();
        gameService.enterGame(game.getId(), "Nora");

        ArgumentCaptor<UnaryOperator> lambdaCaptor = ArgumentCaptor.forClass(UnaryOperator.class);
        verify(gameEntityService).editGame(any(), lambdaCaptor.capture());
        lambdaCaptor.getValue().apply(game);

        Map<String, Player> tempWait = game.getWaitingRoom();

        assertEquals(1, tempWait.size());
        assertTrue(tempWait.keySet().stream().map(tempWait::get).anyMatch(player -> Objects.equals(player.getName(), "Nora")));
    }

    @Test
    void enterGameWithExistingPlayerOfSameName() {
        Game game = mockGameInRepository();
        gameService.enterGame(game.getId(), "Nora");

        ArgumentCaptor<UnaryOperator> lambdaCaptor = ArgumentCaptor.forClass(UnaryOperator.class);
        verify(gameEntityService).editGame(any(), lambdaCaptor.capture());
        lambdaCaptor.getValue().apply(game);

        gameService.enterGame(game.getId(), "Nora");
        verify(gameEntityService, times(2)).editGame(any(), lambdaCaptor.capture());
        lambdaCaptor.getValue().apply(game);

        Map<String, Player> tempWait = game.getWaitingRoom();
        assertEquals(2, tempWait.size());
        assertTrue(tempWait.keySet().stream().map(tempWait::get).anyMatch(player -> Objects.equals(player.getName(), "Nora1360")));

    }
}