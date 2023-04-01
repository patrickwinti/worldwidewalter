package ch.zhaw.www;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Prompt;
import ch.zhaw.www.model.Round;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class TestHelper {
    private static final Prompt PROMPT = new Prompt("I am WALTER", 1);
    public static final Duration DEFAULT_PROPOSITION_DURATION = Duration.of(5, ChronoUnit.MINUTES);
    public static final Duration DEFAULT_PROPOSITION_END_DURATION = Duration.of(1, ChronoUnit.MINUTES);
    public static final Duration DEFAULT_SUBMISSION_DURATION = Duration.of(1, ChronoUnit.MINUTES);
    
    public static Player addToWaitingRoom(Game game) {
        Player player = createPlayer();
        game.addPlayerToWaitingRoom(player);
        return player;
    }
    
    public static void addRoundOpenForPropositionSubmission(Game game) {
        Round round = createRound();
        round.setSphinx(getRandomPlayer(game));
        game.addRound(round);
    }
    
    public static Player createPlayer() {
        return new Player(getId(), "Chris");
    }
    
    public static Player getRandomPlayer(Game game) {
        return game.getAllPlayers().findFirst().get();
    }
    
    public static void addToActive(Game game) {
        Player player = createPlayer();
        game.addPlayerToWaitingRoom(player);
        game.markPlayerAsActive(player);
    }
    
    public static Round createRound(Duration duration) {
        return new Round(getId(), PROMPT, duration, DEFAULT_PROPOSITION_END_DURATION, DEFAULT_SUBMISSION_DURATION);
    }
    
    public static Round createRound() {
        return createRound(DEFAULT_PROPOSITION_DURATION);
    }
    
    public static Game createGame() {
        return new Game(getId(), 4, 12, 1);
    }
    
    public static Game createGame(String id) {
        return new Game(id, 4, 12, 1);
    }
    
    private static String getId() {
        return UUID.randomUUID().toString();
    }
    
}
