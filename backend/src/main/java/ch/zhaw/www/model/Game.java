package ch.zhaw.www.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

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
    @Id
    @Getter
    @NotNull
    private final String id;
    private final int minimumAmountOfPlayers;
    private final int maximumAmountOfPlayers;
    private final int numberOfRoundsInTurn;
    private final List<Round> rounds = new ArrayList<>();
    private final Map<String, Player> activePlayers = new HashMap<>();
    private final List<Player> players = new ArrayList<>();
    private boolean roundReachedMinimumPlayers = false;
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
        return round.getNumberOfSelectionsSubmitted() < max(getConnectedActivePlayersCount(), MIN_PLAYERS_TO_CONTINUE) - 1;
    }

    private boolean isMissingPlayerProposition(final Round round) {
        return round.getNumberOfPropositionsSubmitted() < max(getConnectedActivePlayersCount(), MIN_PLAYERS_TO_CONTINUE);
    }

    private long getConnectedActivePlayersCount() {
        return activePlayers.values().stream().filter(Player::isConnected).count();
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
            return getConnectedActivePlayersCount() >= MIN_PLAYERS_TO_CONTINUE;
        }
        return activePlayers.size() >= minimumAmountOfPlayers;
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
        activePlayers.clear();
        roundReachedMinimumPlayers = false;
        rounds.add(round);
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
        players.stream().filter(p -> p.getId().equals(playerId)).findFirst()
                .ifPresent(p -> p.setConnected(false));
        Player activePlayer = activePlayers.get(playerId);
        if (activePlayer != null) {
            activePlayer.setConnected(false);
        }
    }

    /**
     * Marks a player as connected again (rejoining)
     *
     * @param playerId player identifier
     */
    public void markPlayerConnected(@NotNull String playerId) {
        players.stream().filter(p -> p.getId().equals(playerId)).findFirst()
                .ifPresent(p -> p.setConnected(true));
        Player activePlayer = activePlayers.get(playerId);
        if (activePlayer != null) {
            activePlayer.setConnected(true);
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