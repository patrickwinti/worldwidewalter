package ch.zhaw.www.service;

import ch.zhaw.www.model.Round;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class GameServiceIntegrationTest {
    
    @Autowired
    private GameService gameService;
    
    @Test
    void enterGame_StartsNewRound() throws InterruptedException {
        String gameId = gameService.createGame().getId();
        
        assertNull(gameService.getGame(gameId).getCurrentRound());
        assertEquals(0, gameService.getGame(gameId).getRounds().size());
        
        int nrOfPlayers = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService service = Executors.newFixedThreadPool(nrOfPlayers);
        var callables = IntStream.range(0, nrOfPlayers)
                .mapToObj(value -> gameService.enterGame(gameId, "Sara" + value))
                .map(player -> (Callable<Round>) () -> gameService.enterRound(gameId, player.getId()))
                .toList();
        
        service.invokeAll(callables);
        service.shutdown();
        
        assertEquals(1, gameService.getGame(gameId).getRounds().size());
    }
    
}