package ch.zhaw.www.model;

import ch.zhaw.www.utils.InstantWrapper;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.max;

/**
 * Class containing all the information related to a game. It controls players in game, running rounds and prompts from
 * the deck
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@KeySpace("running_games")
public class Game {
    private static final int MIN_PLAYERS_TO_CONTINUE = 3;
    /**
     * How long a disconnected player is still counted as present. A shorter connection hiccup
     * does not drop them from the round wait or the minimum-player check; only once they have
     * been gone for longer than this are they ignored.
     */
    private static final Duration DISCONNECT_GRACE = Duration.ofSeconds(10);
    @Id
    @Getter
    @NotNull
    private final String id;
    @Getter
    private final int minimumAmountOfPlayers;
    private final int maximumAmountOfPlayers;
    private final int numberOfRoundsInTurn;
    private final List<Round> rounds = new ArrayList<>();
    private final Map<String, Player> activePlayers = new HashMap<>();
    private final List<Player> players = new ArrayList<>();
    private boolean roundReachedMinimumPlayers = false;
    // Number of players who took part in the previous round; the next round waits for this many
    // to re-enter (click "Weiter") before it starts. Zero for the very first round.
    private int expectedContinueCount = 0;
    @Getter
    @Setter
    @Nullable
    private String hostId;
    @Getter
    private boolean started = false;
    @Setter
    private Map<Player, Integer> sphinxCandidates = new HashMap<>();
    private final List<Prompt> prompts;
    @Getter
    private final Map<String, Integer> points = new HashMap<>();
    @Setter
    @Getter
    private Instant lastEdit;
    
    /**
     * Gets current round, when the SphinxElector has been selected
     *
     * @return {@link Round} or null
     */
    @Nullable
    public Round getCurrentRound() {
        if (rounds.isEmpty()) {
            return null;
        } else {
            return rounds.getLast();
        }
    }
    
    /**
     * Existing rounds in current game
     *
     * @return stream of all rounds
     */
    public Stream<Round> getRoundHistory() {
        return rounds.stream();
    }
    
    /**
     * Gets current round
     *
     * @return Optional {@link Round}
     */
    public Optional<Round> getCurrentRoundOptional() {
        return Optional.ofNullable(getCurrentRound());
    }
    
    /**
     * Check if there is an open round that is finished or if there is no round at all
     *
     * @return true if there is no round or it is finished
     */
    public boolean needsNewRound() {
        return getCurrentRoundOptional()
                .map(this::isRoundFinished)
                .orElse(true);
    }
    
    /**
     * Check if there is an open round that is finished or if there is no round at all
     *
     * @param round round to check
     * @return true if there is no round or it is finished
     */
    public boolean isRoundFinished(Round round) {
        return round.isFinished() || !isMissingPlayerSelections(round);
    }
    
    /**
     * Checks whether player can enter current round or they will need to wait for another round
     *
     * @return true if the round has capacity, and it is still open for propositions
     */
    public boolean canRoundBeEntered() {
        return getCurrentRoundOptional()
                .map(round -> round.canEnterRound() && isMissingPlayerProposition(round) && hasCapacityForNewActivePlayer())
                .orElse(false);
    }
    
    /**
     * Verifies game can accept any proposition sent
     *
     * @return true if round has not yet receive a proposition submission from each active player
     */
    public boolean canAcceptPropositionsForCurrentRound() {
        return getCurrentRoundOptional()
                .map(this::canAcceptPropositionsForRound)
                .orElse(false);
    }
    
    /**
     * Verifies game can accept any proposition sent
     *
     * @return true if round has not yet receive a proposition submission from each active player
     */
    public boolean canAcceptPropositionsForRound(Round round) {
        return hasEnoughPlayers() && round.acceptsPropositions() && isMissingPlayerProposition(round);
    }
    
    /**
     * Verifies game can accept send selections
     *
     * @param round round to check status on
     * @return true if round has not yet receive a selections submission from each active player but the sphinx
     */
    public boolean canAcceptSelectionForRound(Round round) {
        return isMissingPlayerSelections(round) &&
                ((round.acceptsPropositions() && !isMissingPlayerProposition(round)) || round.acceptsSelections());
    }
    
    private boolean isMissingPlayerSelections(final Round round) {
        // Everyone but the sphinx selects, hence the -1.
        return round.getNumberOfSelectionsSubmitted() < getExpectedResponderCount() - 1;
    }

    private boolean isMissingPlayerProposition(final Round round) {
        return round.getNumberOfPropositionsSubmitted() < getExpectedResponderCount();
    }

    /**
     * Number of players the round waits for before it advances to the next phase.
     * <p>
     * This counts every active player still present in the game — including a player with a
     * brief connection drop, who remains an active player until they actually leave. A player
     * who leaves is removed from the active players and therefore no longer blocks the round;
     * the per-phase timer remains the final backstop.
     *
     * @return the number of players expected to respond in the current round
     */
    private long getExpectedResponderCount() {
        return max(getPresentActivePlayersCount(), MIN_PLAYERS_TO_CONTINUE);
    }

    /**
     * Number of active players still counted as present: connected players plus those whose
     * disconnect is still within the {@link #DISCONNECT_GRACE} window. A player gone for longer
     * than the grace period is ignored, so a dead connection no longer blocks the round.
     *
     * @return the number of present active players
     */
    private long getPresentActivePlayersCount() {
        Instant now = InstantWrapper.getNow();
        return activePlayers.values().stream()
                .filter(player -> player.isPresent(now, DISCONNECT_GRACE))
                .count();
    }

    /**
     * Has enough players to continue with the current round.
     * Once a round has reached the minimum number of players, it can continue as long as
     * at least MIN_PLAYERS_TO_CONTINUE (3) connected active players remain.
     *
     * @return true if there are enough players to proceed
     */
    public boolean hasEnoughPlayers() {
        if (roundReachedMinimumPlayers) {
            return getPresentActivePlayersCount() >= MIN_PLAYERS_TO_CONTINUE;
        }
        return activePlayers.size() >= minimumAmountOfPlayers;
    }

    /**
     * Marks the game as started by the host. After this the first round may be created.
     */
    public void markStarted() {
        this.started = true;
    }

    /**
     * Checks whether the given player is the host of this game.
     *
     * @param playerId player identifier
     * @return true if the player is the current host
     */
    public boolean isHost(@NotNull String playerId) {
        return playerId.equals(hostId);
    }

    /**
     * @return the host player if one is set and still in the game, otherwise empty
     */
    public Optional<Player> getHost() {
        if (hostId == null) {
            return Optional.empty();
        }
        return players.stream().filter(player -> player.getId().equals(hostId)).findFirst();
    }

    /**
     * @return true if the host is still present (in the game and connected, or within the disconnect grace)
     */
    public boolean isHostPresent() {
        Instant now = InstantWrapper.getNow();
        return getHost().map(host -> host.isPresent(now, DISCONNECT_GRACE)).orElse(false);
    }

    /**
     * Players who are still present in the game (connected or within the disconnect grace). Used
     * for the lobby: the start check and for choosing a replacement host.
     *
     * @return the present players
     */
    public List<Player> getPresentPlayers() {
        Instant now = InstantWrapper.getNow();
        return players.stream()
                .filter(player -> player.isPresent(now, DISCONNECT_GRACE))
                .toList();
    }

    /**
     * @return true if enough players are present in the lobby for the host to start the game
     */
    public boolean hasEnoughPlayersToStart() {
        return getPresentPlayers().size() >= minimumAmountOfPlayers;
    }
    
    /**
     * Returns the first prompt in the list of prompts and removes it from the list.
     * <p>
     * Note: The implementation currently returns the same prompt every time, as the deck has not yet been implemented.
     *
     * @return the first prompt in the list of prompts
     */
    public Prompt consumePrompt() {
        return prompts.get(rounds.size() % prompts.size());
    }
    
    /**
     * Adds a new round to the game.
     *
     * @param round the round to add
     */
    public void addRound(Round round) {
        // Remember how many players were in the round we are leaving, so the new round can wait
        // for all of them to re-enter before it starts (0 for the first round of the game).
        this.expectedContinueCount = (int) getPresentActivePlayersCount();
        activePlayers.clear();
        roundReachedMinimumPlayers = false;
        rounds.add(round);
    }

    /**
     * Whether everyone who is expected to play the current round has entered it. For the first
     * round this is always true (so the usual minimum-player check governs the start); for later
     * rounds the round waits until all present players of the previous round have re-entered.
     * The count is capped by the players still present, so a player who left cannot block the start.
     *
     * @return true if all expected players have entered the current round
     */
    public boolean allExpectedPlayersEntered() {
        int expected = Math.min(expectedContinueCount, getPresentPlayers().size());
        return getPresentActivePlayersCount() >= expected;
    }
    
    /**
     * Returns a stream of all players in the game, including those in the waiting room and those who are active.
     *
     * @return a stream of all players in the game
     */
    public Stream<Player> getAllPlayers() {
        return players.stream();
    }
    
    /**
     * Fetches the name of a player
     *
     * @param id player id
     * @return name for given player id or null if not found
     */
    @Nullable
    public String getPlayerNameFromId(String id) {
        return getAllPlayers()
                .filter(player -> player.getId().equals(id))
                .map(Player::getName)
                .findFirst().orElse(null);
    }
    
    /**
     * Moves players in the waiting room into the active list, if there is space in current round
     *
     * @param player player that will be marked as active
     */
    public void moveToActivePlayers(Player player) {
        activePlayers.put(player.getId(), player);
        if (activePlayers.size() >= minimumAmountOfPlayers) {
            roundReachedMinimumPlayers = true;
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
     * Removes player from waiting room or active players
     *
     * @param playerId player identifier
     */
    public void removePlayer(@NotNull String playerId) {
        players.removeIf(player -> player.getId().equals(playerId));
        activePlayers.remove(playerId);
        points.remove(playerId);
        sphinxCandidates.keySet().stream()
                .filter(integer -> integer.getId().equals(playerId))
                .findFirst().ifPresent(sphinxCandidates::remove);
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
     * Fetches sphinx candidates. If there are no candidates, then it returns a set of entries.
     *
     * @return map of players and rounds per player
     */
    public Map<Player, Integer> getSphinxCandidates() {
        if (sphinxCandidates.isEmpty()) {
            sphinxCandidates = players.stream()
                    .map(player -> Map.entry(player, numberOfRoundsInTurn))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return new HashMap<>(sphinxCandidates);
    }
    
    /**
     * Adds and stores the points of the players in the game.
     *
     * @param evaluation a map containing the playerId as key and points as value
     */
    public void addPoints(Map<String, Integer> evaluation) {
        evaluation.forEach((k, v) -> points.merge(k, v, Integer::sum));
    }
    
    /**
     * Marks a player as disconnected without removing them from the game
     *
     * @param playerId player identifier
     */
    public void markPlayerDisconnected(@NotNull String playerId) {
        Instant now = InstantWrapper.getNow();
        players.stream().filter(p -> p.getId().equals(playerId)).findFirst()
                .ifPresent(p -> p.markDisconnected(now));
        Player activePlayer = activePlayers.get(playerId);
        if (activePlayer != null) {
            activePlayer.markDisconnected(now);
        }
    }

    /**
     * Marks a player as connected again (rejoining)
     *
     * @param playerId player identifier
     */
    public void markPlayerConnected(@NotNull String playerId) {
        players.stream().filter(p -> p.getId().equals(playerId)).findFirst()
                .ifPresent(Player::markConnected);
        Player activePlayer = activePlayers.get(playerId);
        if (activePlayer != null) {
            activePlayer.markConnected();
        }
    }

    /**
     * Checks if a player is registered and currently connected
     *
     * @param playerId player identifier
     * @return true if the player is registered and connected
     */
    public boolean isPlayerConnected(@NotNull String playerId) {
        return players.stream()
                .anyMatch(p -> p.getId().equals(playerId) && p.isConnected());
    }

    /**
     * Adds player to game and as a sphinx candidate
     *
     * @param player new player entering
     */
    public void registerPlayer(Player player) {
        points.put(player.getId(), 0);
        players.add(player);
        sphinxCandidates.put(player, numberOfRoundsInTurn);
    }
}