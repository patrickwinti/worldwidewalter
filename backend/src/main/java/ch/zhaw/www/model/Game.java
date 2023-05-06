package ch.zhaw.www.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

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
    @Id
    @Getter
    @NotNull
    private final String id;
    private final int minimumAmountOfPlayers;
    private final int maximumAmountOfPlayers;
    private final int numberOfRoundsInTurn;
    private final List<Round> rounds = new ArrayList<>();
    private final Map<String, Player> activePlayers = new HashMap<>();
    @Setter
    private Set<Map.Entry<Player, Integer>> sphinxCandidates = new HashSet<>();
    private final List<Prompt> prompts;
    @Getter
    private final Map<Player, Integer> points = new HashMap<>();
    
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
            return rounds.get(rounds.size() - 1);
        }
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
                .map(round -> round.isFinished() || !isMissingPlayerSelections(round))
                .orElse(true);
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
    public boolean canAcceptPropositions() {
        return getCurrentRoundOptional()
                .map(round -> hasEnoughPlayers() && round.acceptsPropositions() && isMissingPlayerProposition(round))
                .orElse(false);
    }
    
    /**
     * Verifies game can accept send selections
     *
     * @return true if round has not yet receive a selections submission from each active player but the sphinx
     */
    public boolean canAcceptSelections() {
        return getCurrentRoundOptional()
                .map(round -> isMissingPlayerSelections(round) &&
                        ((round.acceptsPropositions() && !isMissingPlayerProposition(round)) || round.acceptsSelections()))
                .orElse(false);
    }
    
    private boolean isMissingPlayerSelections(final Round round) {
        return round.getNumberOfSelectionsSubmitted() < max(activePlayers.size(), minimumAmountOfPlayers) - 1;
    }
    
    private boolean isMissingPlayerProposition(final Round round) {
        return round.getNumberOfPropositionsSubmitted() < max(activePlayers.size(), minimumAmountOfPlayers);
    }
    
    private boolean hasEnoughPlayers() {
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
        rounds.add(round);
    }
    
    /**
     * Returns a stream of all players in the game, including those in the waiting room and those who are active.
     *
     * @return a stream of all players in the game
     */
    public Stream<Player> getAllPlayers() {
        return points.keySet().stream();
    }
    
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
        sphinxCandidates.stream().filter(entry -> entry.getKey().getId().equals(playerId))
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
     * @return set of every map entry
     */
    public Set<Map.Entry<Player, Integer>> getSphinxCandidates() {
        if (sphinxCandidates.isEmpty()) {
            sphinxCandidates = activePlayers.values().stream()
                    .map(player -> Map.entry(player, numberOfRoundsInTurn))
                    .collect(Collectors.toSet());
        }
        return sphinxCandidates;
    }
    
    /**
     * Adds and stores the points of the players in the game.
     *
     * @param evaluation a map containing the playerId as key and points as value
     */
    public void addPoints(Map<String, Integer> evaluation) {
        points.entrySet()
                .stream()
                .filter(entry -> evaluation.containsKey(entry.getKey().getId()))
                .forEach(entry -> {
                    int pointsToAdd = evaluation.getOrDefault(entry.getKey().getId(), 0);
                    points.put(entry.getKey(), entry.getValue() + pointsToAdd);
                });
    }
    
    /**
     * Adds player to game and as a sphinx candidate
     *
     * @param player new player entering
     */
    public void registerPlayer(Player player) {
        points.put(player, 0);
        sphinxCandidates.add(Map.entry(player, numberOfRoundsInTurn));
    }
}