package ch.zhaw.www.model;

import ch.zhaw.www.utils.InstantWrapper;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    private static final Prompt PROMPT = new Prompt("I am WALTER", 1);
    private static final int DURATION = 4;
    
    private static Round getRound(int propositionDuration) {
        return new Round(getId(), PROMPT, propositionDuration, 1);
    }
    
    private static void addToWaitingRoom(Game game) {
        Player player = getPlayer();
        game.getWaitingRoom().put(player.getId(), player);
    }
    
    private static void addToActive(Game game) {
        Player player = getPlayer();
        game.getActivePlayers().put(player.getId(), player);
    }
    
    private static Player getPlayer() {
        return new Player(getId());
    }
    
    private static String getId() {
        return UUID.randomUUID().toString();
    }
    
    private static void addRoundOpenForPropositionSubmission(Game game) {
        Round round = getRound(DURATION);
        game.newRound(round);
        game.getActivePlayers().putAll(game.getWaitingRoom());
        round.setSphinx(game.getActivePlayers().values().iterator().next());
        round.openForPropositionSubmission();
    }
    
    @Test
    void testGameState_WaitingForPlayers() {
        Game game = new Game(getId());
        assertEquals(Game.State.NO_VALID_ROUND, game.getGameState());
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        
        game.getActivePlayers().putAll(game.getWaitingRoom());
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        
        Round round = getRound(2);
        game.newRound(round);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        
        round.setSphinx(game.getWaitingRoom().values().iterator().next());
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
    }
    
    @Test
    void testGameState_WaitingForPropositions() {
        Game game = new Game(getId());
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        
        addRoundOpenForPropositionSubmission(game);
        assertEquals(Game.State.WAITING_FOR_ALL_PROPOSITIONS, game.getGameState());
    }
    
    @Test
    void testGameState_WaitingForSelections() {
        Game game = new Game(getId());
        addToActive(game);
        addToActive(game);
        addToActive(game);
        addToActive(game);
        
        addRoundOpenForPropositionSubmission(game);
        var round = game.getRunningRound();
        assertNotNull(round);
        game.getActivePlayers().forEach((s, player) -> round.getPropositions().put(s, "Walter " + player.getId()));
        assertEquals(Game.State.WAITING_FOR_SELECTIONS, game.getGameState());
        
        InstantWrapper.clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        addRoundOpenForPropositionSubmission(game);
        InstantWrapper.clock = Clock.offset(InstantWrapper.clock, Duration.of(DURATION, ChronoUnit.MINUTES));
        var player = game.getActivePlayers().values().stream().findAny().get();
        game.getRunningRound().getPropositions().put(player.getId(), "Walter " + player.getId());
        assertEquals(Game.State.WAITING_FOR_SELECTIONS, game.getGameState());
        
    }
    
    @Test
    void testRunningRound() {
        Game game = new Game(getId());
        assertNull(game.getRunningRound());
        
        Round round = getRound(3);
        game.newRound(round);
        assertNull(game.getRunningRound());
        
        Player sphinx = getPlayer();
        round.setSphinx(sphinx);
        
        assertSame(round, game.getRunningRound());
    }
    
    @Test
    void testNewRound() {
        Game game = new Game(getId());
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        addToWaitingRoom(game);
        assertEquals(4, game.getWaitingRoom().size());
        addToActive(game);
        addToActive(game);
        addToActive(game);
        addToActive(game);
        assertEquals(4, game.getActivePlayers().size());
        
        game.newRound(getRound(2));
        
        assertEquals(8, game.getWaitingRoom().size());
        assertEquals(0, game.getActivePlayers().size());
    }
}