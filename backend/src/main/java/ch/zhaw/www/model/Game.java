package ch.zhaw.www.model;

import ch.zhaw.www.service.GameError;
import ch.zhaw.www.service.PlayerError;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final List<Prompt> prompts = List.of(new Prompt("I've always wanted to WALTER", 1));
    
    public void addRound(Round round) {
        rounds.add(round);
    }
    
    /**
     * Gets current round,
     * when the Sphinx has been selected
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
        var numberOfActivePlayers = activePlayers.size();
        if (round == null) {
            return State.NO_VALID_ROUND;
        } else if (numberOfActivePlayers < minimumAmountOfPlayers || round.getState() == Round.State.CREATED) {
            return State.WAITING_FOR_PLAYERS;
        } else if (round.getState() == Round.State.OPEN_FOR_SUBMISSIONS &&
                round.canEnterRound() &&
                round.getNumberOfPropositionsSubmitted() < numberOfActivePlayers) {
            return State.WAITING_FOR_ALL_PROPOSITIONS;
        } else if ((round.getState() == Round.State.OPEN_FOR_SUBMISSIONS ||
                round.getState() == Round.State.OPEN_FOR_SELECTIONS) &&
                round.getNumberOfSelectionsSubmitted() < numberOfActivePlayers) {
            return State.WAITING_FOR_ALL_SELECTIONS;
        } else {
            return State.NO_VALID_ROUND;
        }
    }
    
    public Prompt consumePrompt() {
        // uncomment when deck is implemented: return prompts.remove(0) ;
        return prompts.get(0);
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
     * @throws GameError.FullCapacityException if more than maximum players reached
     */
    public void moveToActivePlayers(Player player) throws GameError.FullCapacityException {
        if (activePlayers.size() < maximumAmountOfPlayers) {
            if (getAllPlayers().noneMatch(p -> p.equals(player))) {
                throw new PlayerError.NotFoundException(id);
            }
            waitingRoom.remove(player.getId());
            activePlayers.put(player.getId(), player);
        } else {
            throw new GameError.FullCapacityException();
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
        if (!hasActivePlayer(player.getId())) {
            waitingRoom.put(player.getId(), player);
        }
    }
/**
     * Removes player from waiting room or active players
     *
     * @param playerId player identifier
     * @throws PlayerError.NotFoundException if player is not in waiting room or active players
     */
    public void removePlayer(@NotNull String playerId) throws PlayerError.NotFoundException{
        if (!hasPlayer(playerId)) {
            throw new PlayerError.NotFoundException(id);
        }
        waitingRoom.remove(playerId);
        activePlayers.remove(playerId);
    }

    
    public enum State {
        NO_VALID_ROUND, WAITING_FOR_PLAYERS, WAITING_FOR_ALL_PROPOSITIONS, WAITING_FOR_ALL_SELECTIONS
    }
}
