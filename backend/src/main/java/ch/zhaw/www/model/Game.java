package ch.zhaw.www.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class containing all the information related to a game.
 * It controls players in game, running rounds and prompts from the deck
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@KeySpace("running_games")
public class Game {
    @Id
    @Getter
    @NotNull
    private final String id;
    private final int minimumAmountOfPlayers;
    private final int maximumAmountOfPlayers;
    private final int numberOfRoundsInTurn;
    private final List<Round> rounds = new ArrayList<>();
    private final Map<String, Player> waitingRoom = new HashMap<>();
    private final Map<String, Player> activePlayers = new HashMap<>();
    @Setter
    private Set<Map.Entry<Player, Integer>> sphinxCandidates;
    private final List<Prompt> prompts = List.of(new Prompt("I've always wanted to WALTER", 1));
    
    /**
     * Gets current round,
     * when the SphinxElector has been selected
     *
     * @return {@link Round} or null
     */
    @Nullable
    public Round getCurrentRound() {
        if (rounds.isEmpty()) {
            return null;
        } else {
            var round = rounds.get(rounds.size() - 1);
            return round.getState() != Round.State.FINISHED ? round : null;
        }
    }
    
    /**
     * Returns state of the current game:
     * - Waiting for player: Not enough players active or no valid round
     * - Waiting for propositions: Not all players did send propositions and is in time for sending any
     * - Waiting for selections: Not all players did send propositions
     *
     * @return {@link Game.State}
     */
    @NotNull
    public State getState() {
        var round = getCurrentRound();
        if (round == null) {
            return State.NO_VALID_ROUND;
        } else if (doesNotHaveEnoughPlayers() || hasRoundNotStartedYet(round)) {
            return State.WAITING_FOR_PLAYERS;
        } else if (round.canEnterRound() && canAcceptPropositions(round)) {
            return State.WAITING_FOR_ALL_PROPOSITIONS;
        } else if (canAcceptSelections(round) && haveAllPlayersSubmittedASelection(round)) {
            return State.WAITING_FOR_ALL_SELECTIONS;
        } else {
            return State.NO_VALID_ROUND;
        }
    }
    
    private boolean haveAllPlayersSubmittedASelection(final Round round) {
        return round.getNumberOfSelectionsSubmitted() == activePlayers.size() - 1;
    }
    
    private boolean canAcceptSelections(final Round round) {
        return round.getState() == Round.State.OPEN_FOR_SUBMISSIONS ||
                round.getState() == Round.State.OPEN_FOR_SELECTIONS;
    }
    
    private boolean doesNotHaveEnoughPlayers() {
        return activePlayers.size() < minimumAmountOfPlayers;
    }
    
    private boolean canAcceptPropositions(final Round round) {
        return round.getNumberOfPropositionsSubmitted() < activePlayers.size() &&
                round.getState() == Round.State.OPEN_FOR_SUBMISSIONS;
    }
    
    private boolean hasRoundNotStartedYet(final Round round) {
        return round.getState() == Round.State.CREATED;
    }
    
    public Prompt consumePrompt() {
        // TODO correct code
        return prompts.get(0);
    }
    
    /**
     * Adds new round to game
     *
     * @param round new round
     */
    public void addRound(Round round) {
        rounds.add(round);
    }
    
    /**
     * Returns stream of all active and waiting room players
     *
     * @return stream with all valid games
     */
    public Stream<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>(waitingRoom.values());
        players.addAll(activePlayers.values());
        return players.stream();
    }
    
    /**
     * Moves players in the waiting room into the active list, if there is space in current round
     *
     * @param player player that will be marked as active
     */
    public void moveToActivePlayers(Player player) {
        waitingRoom.remove(player.getId());
        activePlayers.put(player.getId(), player);
        if (sphinxCandidates != null) {
            sphinxCandidates.stream()
                    .filter(entry -> entry.getKey().equals(player))
                    .findFirst()
                    .ifPresentOrElse(entry -> {
                    }, () -> sphinxCandidates.add(Map.entry(player, numberOfRoundsInTurn)));
        }
    }
    
    /**
     * Checks if player ID is currently an active player
     *
     * @param playerId player identifier
     * @return is an active player or false if in waiting room or not existing
     */
    public boolean hasActivePlayer(@NotNull String playerId) {
        return activePlayers.containsKey(playerId);
    }
    
    /**
     * Checks if player ID is currently a player
     *
     * @param playerId player identifier
     * @return is a player either in waiting room or is active
     */
    public boolean hasPlayer(@NotNull String playerId) {
        return getAllPlayers().anyMatch(player -> player.getId().equals(playerId));
    }
    
    /**
     * Adds player to waiting room if not already active
     *
     * @param player player that shall be added
     */
    public void addPlayerToWaitingRoom(@NotNull Player player) {
        waitingRoom.put(player.getId(), player);
    }
    
    /**
     * Removes player from waiting room or active players
     *
     * @param playerId player identifier
     */
    public void removePlayer(@NotNull String playerId) {
        waitingRoom.remove(playerId);
        activePlayers.remove(playerId);
        if (sphinxCandidates != null) {
            sphinxCandidates.stream().filter(entry -> entry.getKey().getId().equals(playerId))
                    .findFirst().ifPresent(sphinxCandidates::remove);
        }
    }
    
    /**
     * There is room for more active player in this or next round
     *
     * @return true if there is space otherwise false
     */
    public boolean hasCapacityForNewActivePlayer() {
        return activePlayers.size() < maximumAmountOfPlayers;
    }
    
    /**
     * Fetches sphinx candidates. If there are no candidates, then it returns
     * a set of entries.
     *
     * @return set of every map entry
     */
    public Set<Map.Entry<Player, Integer>> getSphinxCandidates() {
        if (sphinxCandidates == null || sphinxCandidates.isEmpty()) {
            sphinxCandidates = activePlayers.values().stream()
                    .map(player -> Map.entry(player, numberOfRoundsInTurn))
                    .collect(Collectors.toSet());
        }
        return sphinxCandidates;
    }
    
    public enum State {
        NO_VALID_ROUND, WAITING_FOR_PLAYERS, WAITING_FOR_ALL_PROPOSITIONS, WAITING_FOR_ALL_SELECTIONS
    }
}
