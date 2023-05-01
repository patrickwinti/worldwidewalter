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
                .map(player -> (Callable<Round>) () -> {
                    gameService.enterRound(gameId, player.getId());
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
        var mariaPlayer = gameService.enterGame(game.getId(), "Maria");
        gameService.enterRound(game.getId(), mariaPlayer.getId());
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRoundOpenForPropositions(game.getId(), mariaPlayer.getId()));
        var jesusPlayerId = gameService.enterGame(game.getId(), "Jesus");
        gameService.enterRound(game.getId(), jesusPlayerId.getId());
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRoundOpenForPropositions(game.getId(), mariaPlayer.getId()));
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRoundOpenForPropositions(game.getId(), jesusPlayerId.getId()));
        var rockyPlayerId = gameService.enterGame(game.getId(), "Rocky");
        gameService.enterRound(game.getId(), rockyPlayerId.getId());
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRoundOpenForPropositions(game.getId(), mariaPlayer.getId()));
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRoundOpenForPropositions(game.getId(), jesusPlayerId.getId()));
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRoundOpenForPropositions(game.getId(), rockyPlayerId.getId()));
        var bellaPlayer = gameService.enterGame(game.getId(), "Bella");
        gameService.enterRound(game.getId(), bellaPlayer.getId());
        var round = gameService.getRoundOpenForPropositions(game.getId(), bellaPlayer.getId());
        assertNotNull(round);
    }
    
    @Test
    void testWaitingRoomWhileRoundRunning() {
        enableFixedClocked();
        var gameId = gameService.createGame().getId();
        var caelanPlayer = gameService.enterGame(gameId, " Caelan");
        var cardeaPlayer = gameService.enterGame(gameId, "Cardea");
        var grifudPlayer = gameService.enterGame(gameId, "Grifud");
        var shulamitPlayer = gameService.enterGame(gameId, "Shulamit");
        var neusPlayer = gameService.enterGame(gameId, "Neus ");
        
        gameService.enterRound(gameId, caelanPlayer.getId());
        gameService.enterRound(gameId, cardeaPlayer.getId());
        gameService.enterRound(gameId, grifudPlayer.getId());
        gameService.enterRound(gameId, shulamitPlayer.getId());
        
        var round1 = gameService.getRoundOpenForPropositions(gameId, shulamitPlayer.getId());
        roundService.submitProposition(round1.getId(), caelanPlayer.getId(), List.of(" Perikles"));
        roundService.submitProposition(round1.getId(), cardeaPlayer.getId(), List.of("Eleonore"));
        
        // can't participate in round anymore
        offsetFixedClockBy(gameProperties.getPropositionSubmissionDuration().minus(gameProperties.getRoundEnterLimitDuration()).plus(1, ChronoUnit.SECONDS));
        gameService.enterRound(gameId, neusPlayer.getId());
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRoundOpenForPropositions(gameId, neusPlayer.getId()));
        
        roundService.submitProposition(round1.getId(), grifudPlayer.getId(), List.of("Lysistrata"));
        roundService.submitProposition(round1.getId(), shulamitPlayer.getId(), List.of("Yedidia"));
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRoundOpenForPropositions(gameId, neusPlayer.getId()));
        
        // Done with selections
        offsetFixedClockBy(gameProperties.getSelectionSubmissionDuration()
                .plus(gameProperties.getPropositionSubmissionDuration())
                .plus(1, ChronoUnit.SECONDS));
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRoundOpenForPropositions(gameId, neusPlayer.getId()));
        gameService.enterRound(gameId, caelanPlayer.getId());     //creates new round
        
        assertThrows(RoundError.IllegalStateException.class, () -> gameService.getRoundOpenForPropositions(gameId, neusPlayer.getId()));
        gameService.enterRound(gameId, cardeaPlayer.getId());     //selects sphinx
        var round2 = gameService.getRoundOpenForPropositions(gameId, neusPlayer.getId());
        
        gameService.enterRound(gameId, grifudPlayer.getId());
        gameService.enterRound(gameId, shulamitPlayer.getId());
        
        var caelanRoundResponse = gameService.getRoundOpenForPropositions(gameId, caelanPlayer.getId());
        var cardeaRoundResponse = gameService.getRoundOpenForPropositions(gameId, cardeaPlayer.getId());
        var grifudRoundResponse = gameService.getRoundOpenForPropositions(gameId, grifudPlayer.getId());
        var shulamitRoundResponse = gameService.getRoundOpenForPropositions(gameId, shulamitPlayer.getId());
        var neusRoundResponse = gameService.getRoundOpenForPropositions(gameId, neusPlayer.getId());
        
        assertNotEquals(round1.getId(), round2.getId());
        assertEquals(round2.getId(), caelanRoundResponse.getId());
        assertEquals(round2.getId(), cardeaRoundResponse.getId());
        assertEquals(round2.getId(), grifudRoundResponse.getId());
        assertEquals(round2.getId(), shulamitRoundResponse.getId());
        assertEquals(round2.getId(), neusRoundResponse.getId());
    }
    
}