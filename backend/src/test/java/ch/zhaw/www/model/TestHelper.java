package ch.zhaw.www.model;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class TestHelper {
    private static final Prompt PROMPT = new Prompt("I am WALTER", 1);
    
    public static Player addToWaitingRoom(Game game) {
        Player player = getPlayer();
        game.getWaitingRoom().put(player.getId(), player);
        return player;
    }
    
    public static void markActivePlayer(Game game, Player player) {
        game.getActivePlayers().put(player.getId(), player);
    }
    
    public static Player getPlayer() {
        return new Player(getId(), "Chris");
    }
    
    public static Player getRandomPlayer(Game game) {
        return game.getAllPlayers().findFirst().get();
    }
    
    public static void addToActive(Game game) {
        Player player = getPlayer();
        game.getActivePlayers().put(player.getId(), player);
    }
    
    public static Round getRound(int propositionDuration) {
        return new Round(getId(), PROMPT, Duration.of(propositionDuration, ChronoUnit.MINUTES), Duration.of(1, ChronoUnit.MINUTES));
    }
    
    public static Round getRound() {
        return getRound(2);
    }
    
    public static Game getGame() {
        return new Game(getId(), 4, 12, 1);
    }
    
    public static Game getGame(String id) {
        return new Game(id, 4, 12, 1);
    }
    
    private static String getId() {
        return UUID.randomUUID().toString();
    }
    
}
