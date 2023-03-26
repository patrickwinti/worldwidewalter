package ch.zhaw.www.service;

import ch.zhaw.www.model.Player;
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
        
        assertNull(gameService.getGame(gameId).getRunningRound());
        assertEquals(0, gameService.getGame(gameId).getRounds().size());
        
        int numberOfThreads = 5;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        
        var callables = IntStream.range(0, numberOfThreads)
                .mapToObj(operand -> (Callable<Player>) () -> gameService.enterGame(gameId, "Enrique"))
                .toList();
        
        service.invokeAll(callables);
        service.shutdown();
        
        assertEquals(1, gameService.getGame(gameId).getRounds().size());
    }
    
}