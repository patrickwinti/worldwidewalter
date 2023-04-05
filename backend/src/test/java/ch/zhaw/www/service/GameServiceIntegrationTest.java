package ch.zhaw.www.service;

import ch.zhaw.www.model.Round;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GameServiceIntegrationTest {

    @Autowired
    private GameService gameService;

    @Test
    void enterGame_StartsNewRound() throws InterruptedException {
        String gameId = gameService.createGame().getId();

        assertNull(gameService.getGame(gameId).getCurrentRound());

        int nrOfPlayers = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService service = Executors.newFixedThreadPool(nrOfPlayers);
        var callables = IntStream.range(0, nrOfPlayers)
                .mapToObj(value -> gameService.enterGame(gameId, "Sara"))
                .map(playerId -> (Callable<Round>) () -> {
                    gameService.enterRound(gameId, playerId);
                    return gameService.getGame(gameId).getCurrentRound();
                })
                .toList();

        var futures = service.invokeAll(callables);
        service.shutdown();

        var distinctRounds = futures.stream()
                .map(roundFuture -> {
                    try {
                        return roundFuture.get();
                    } catch (Throwable e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        assertEquals(1, distinctRounds.size());
        assertNotNull(distinctRounds.get(0));
    }

    @Test
    void testStartOfGame() {
        var game = gameService.createGame();
        var mariaPlayerId = gameService.enterGame(game.getId(), "Maria");
        var jesusPlayerId = gameService.enterGame(game.getId(), "Jesus");
        var rockyPlayerId = gameService.enterGame(game.getId(), "Rocky");
        var bellaPlayerId = gameService.enterGame(game.getId(), "Bella");
        gameService.enterRound(game.getId(), mariaPlayerId);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(game.getId(), mariaPlayerId));
        gameService.enterRound(game.getId(), jesusPlayerId);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(game.getId(), mariaPlayerId));
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(game.getId(), jesusPlayerId));
        gameService.enterRound(game.getId(), rockyPlayerId);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(game.getId(), mariaPlayerId));
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(game.getId(), jesusPlayerId));
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(game.getId(), rockyPlayerId));
        gameService.enterRound(game.getId(), bellaPlayerId);
        var round = gameService.getCurrentRoundInGame(game.getId(), bellaPlayerId);
        assertNotNull(round);
    }
}