package ch.zhaw.www;

import ch.zhaw.www.model.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestHelper {
    public static final String WALTER_MARKER = "<<walter>>";
    private static final Prompt PROMPT = new Prompt("I am " + WALTER_MARKER, Collections.singletonList("WALTER"));
    public static final Duration DEFAULT_PROPOSITION_DURATION = Duration.of(5, ChronoUnit.MINUTES);
    public static final Duration DEFAULT_PROPOSITION_END_DURATION = Duration.of(1, ChronoUnit.MINUTES);
    public static final Duration DEFAULT_SUBMISSION_DURATION = Duration.of(1, ChronoUnit.MINUTES);
    public static final Duration DEFAULT_CONTINUE_WAIT_DURATION = Duration.of(45, ChronoUnit.SECONDS);
    public static final int MIN_NUMBER_OF_PLAYERS = 4;
    public static final int MAX_NUMBER_OF_PLAYERS = 15;
    public static final int NUMBER_OF_ROUNDS_PER_TURN = 1;
    public static final List<Prompt> PROMPTS = List.of(new Prompt(WALTER_MARKER + " ist cool", Collections.singletonList("WALTER")));
    
    public static Player registerPlayer(Game game) {
        Player player = createPlayer();
        game.registerPlayer(player);
        return player;
    }
    
    public static Player createPlayer() {
        return new Player(getId(), "Chris");
    }
    
    public static Player createPlayer(String playerName) {
        return new Player(getId(), playerName);
    }
    
    public static Player getRandomPlayer(Game game) {
        return game.getAllPlayers().findAny().orElseThrow();
    }
    
    public static void addActivePlayer(Game game) {
        Player player = createPlayer();
        game.registerPlayer(player);
        game.moveToActivePlayers(player);
    }
    
    public static Proposition createProposition(String playerId, String gap) {
        var proposition = new Proposition(UUID.randomUUID().toString(), List.of(gap));
        proposition.submittedBy(playerId);
        return proposition;
    }
    
    public static Proposition createDoubleSubmissionProposition(String playerId, String secondPlayerId, String gap) {
        var proposition = new Proposition(UUID.randomUUID().toString(), List.of(gap));
        proposition.submittedBy(playerId);
        proposition.submittedBy(secondPlayerId);
        return proposition;
    }
    
    public static Proposition createProposition(String playerId, String gap1, String gap2) {
        var proposition = new Proposition(UUID.randomUUID().toString(), List.of(gap1, gap2));
        proposition.submittedBy(playerId);
        return proposition;
    }
    
    public static Round createRound(Duration duration) {
        return new Round(getId(), PROMPT, duration, DEFAULT_PROPOSITION_END_DURATION, DEFAULT_SUBMISSION_DURATION,
                DEFAULT_CONTINUE_WAIT_DURATION);
    }
    
    public static Round createRound() {
        return createRound(DEFAULT_PROPOSITION_DURATION);
    }
    
    public static Game createGame() {
        return createGame(UUID.randomUUID().toString());
    }
    
    public static Game createGame(String id) {
        return new Game(id, MIN_NUMBER_OF_PLAYERS, MAX_NUMBER_OF_PLAYERS, NUMBER_OF_ROUNDS_PER_TURN, PROMPTS);
    }
    
    public static Game createGame(int roundsPerTurn) {
        return new Game(UUID.randomUUID().toString(), MIN_NUMBER_OF_PLAYERS, MAX_NUMBER_OF_PLAYERS, roundsPerTurn, PROMPTS);
    }
    
    private static String getId() {
        return UUID.randomUUID().toString();
    }
    
}
