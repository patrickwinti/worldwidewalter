package ch.zhaw.www.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    private static final Prompt PROMPT = new Prompt("I am WALTER", 1);
    
    private static void addPlayer(Game game) {
        Player player = new Player(getId());
        game.getActivePlayers().put(player.getId(), player);
    }
    
    private static String getId() {
        return UUID.randomUUID().toString();
    }
    
    @Test
    void testGameState() {
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
        
        round.openForPropositionSubmission();
        assertEquals(Game.State.WAITING_FOR_ALL_PROPOSITIONS, game.getGameState());
        
        game.getActivePlayers().forEach((s, player) -> round.getPropositions().put(s, "Walter " + player.getId()));
        
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