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
    
    private static void addPlayer(Game game) {
        Player player = new Player(getId());
        game.getActivePlayers().put(player.getId(), player);
    }
    
    private static String getId() {
        return UUID.randomUUID().toString();
    }
    
    private static void addRoundOpenForPropositionSubmission(Game game) {
        Round round = new Round(getId(), PROMPT, DURATION, 1);
        game.addRound(round);
        round.setSphinx(game.getActivePlayers().values().iterator().next());
        round.openForPropositionSubmission();
    }
    
    @Test
    void testGameState_WaitingForPlayers() {
        Game game = new Game(getId());
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        addPlayer(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        addPlayer(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        addPlayer(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        addPlayer(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        
        Round round = new Round(getId(), PROMPT, 2, 1);
        game.addRound(round);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        
        round.setSphinx(game.getActivePlayers().values().iterator().next());
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
    }
    
    @Test
    void testGameState_WaitingForPropositions() {
        Game game = new Game(getId());
        addPlayer(game);
        addPlayer(game);
        addPlayer(game);
        addPlayer(game);
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.getGameState());
        
        addRoundOpenForPropositionSubmission(game);
        assertEquals(Game.State.WAITING_FOR_ALL_PROPOSITIONS, game.getGameState());
    }
    
    @Test
    void testGameState_WaitingForSelections() {
        Game game = new Game(getId());
        addPlayer(game);
        addPlayer(game);
        addPlayer(game);
        addPlayer(game);
        
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
        
        Round round = new Round(getId(), PROMPT, 3, 1);
        game.addRound(round);
        assertNull(game.getRunningRound());
        
        Player sphinx = new Player(getId());
        round.setSphinx(sphinx);
        
        assertSame(round, game.getRunningRound());
    }
    
}