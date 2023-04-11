package ch.zhaw.www.service;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Round;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static ch.zhaw.www.TimeHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"game.maximum-players=200"})
class GameServiceIntegrationTest {
    
    @Autowired
    private GameService gameService;
    @Autowired
    private RoundService roundService;
    @Autowired
    private GameProperties gameProperties;
    
    @AfterEach
    void tearDown() {
        disableFixedClocked();
    }
    
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
        gameService.enterRound(game.getId(), mariaPlayerId);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(game.getId(), mariaPlayerId));
        var jesusPlayerId = gameService.enterGame(game.getId(), "Jesus");
        gameService.enterRound(game.getId(), jesusPlayerId);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(game.getId(), mariaPlayerId));
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(game.getId(), jesusPlayerId));
        var rockyPlayerId = gameService.enterGame(game.getId(), "Rocky");
        gameService.enterRound(game.getId(), rockyPlayerId);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(game.getId(), mariaPlayerId));
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(game.getId(), jesusPlayerId));
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(game.getId(), rockyPlayerId));
        var bellaPlayerId = gameService.enterGame(game.getId(), "Bella");
        gameService.enterRound(game.getId(), bellaPlayerId);
        var round = gameService.getCurrentRoundInGame(game.getId(), bellaPlayerId);
        assertNotNull(round);
    }
    
    @Test
    void testWaitingRoomWhileRoundRunning() {
        enableFixedClocked();
        var gameId = gameService.createGame().getId();
        var caelanPlayerId = gameService.enterGame(gameId, " Caelan");
        var cardeaPlayerId = gameService.enterGame(gameId, "Cardea");
        var grifudPlayerId = gameService.enterGame(gameId, "Grifud");
        var shulamitPlayerId = gameService.enterGame(gameId, "Shulamit");
        var neusPlayerId = gameService.enterGame(gameId, "Neus ");
        
        gameService.enterRound(gameId, caelanPlayerId);
        gameService.enterRound(gameId, cardeaPlayerId);
        gameService.enterRound(gameId, grifudPlayerId);
        gameService.enterRound(gameId, shulamitPlayerId);
        
        var round1 = gameService.getCurrentRoundInGame(gameId, shulamitPlayerId);
        roundService.submitProposition(round1.getId(), caelanPlayerId, List.of(" Perikles"));
        roundService.submitProposition(round1.getId(), cardeaPlayerId, List.of("Eleonore"));
        
        // can't participate in round anymore
        offsetFixedClockBy(gameProperties.getPropositionSubmissionDuration().minus(gameProperties.getRoundEnterLimitDuration()).plus(1, ChronoUnit.SECONDS));
        gameService.enterRound(gameId, neusPlayerId);
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(gameId, neusPlayerId));
        
        roundService.submitProposition(round1.getId(), grifudPlayerId, List.of("Lysistrata"));
        roundService.submitProposition(round1.getId(), shulamitPlayerId, List.of("Yedidia"));
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(gameId, neusPlayerId));
        
        // Done with selections
        offsetFixedClockBy(gameProperties.getSelectionSubmissionDuration()
                .plus(gameProperties.getPropositionSubmissionDuration())
                .plus(1, ChronoUnit.SECONDS));
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(gameId, neusPlayerId));
        gameService.enterRound(gameId, caelanPlayerId);     //creates new round
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getCurrentRoundInGame(gameId, neusPlayerId));
        gameService.enterRound(gameId, cardeaPlayerId);     //selects sphinx
        var round2 = gameService.getCurrentRoundInGame(gameId, neusPlayerId);
        
        gameService.enterRound(gameId, grifudPlayerId);
        gameService.enterRound(gameId, shulamitPlayerId);
        
        var caelanRoundResponse = gameService.getCurrentRoundInGame(gameId, caelanPlayerId);
        var cardeaRoundResponse = gameService.getCurrentRoundInGame(gameId, cardeaPlayerId);
        var grifudRoundResponse = gameService.getCurrentRoundInGame(gameId, grifudPlayerId);
        var shulamitRoundResponse = gameService.getCurrentRoundInGame(gameId, shulamitPlayerId);
        var neusRoundResponse = gameService.getCurrentRoundInGame(gameId, neusPlayerId);
        
        assertNotEquals(round1.getId(), round2.getId());
        assertEquals(round2.getId(), caelanRoundResponse.getId());
        assertEquals(round2.getId(), cardeaRoundResponse.getId());
        assertEquals(round2.getId(), grifudRoundResponse.getId());
        assertEquals(round2.getId(), shulamitRoundResponse.getId());
        assertEquals(round2.getId(), neusRoundResponse.getId());
    }
    
}