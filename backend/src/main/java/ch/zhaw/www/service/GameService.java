package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Round;
import jakarta.validation.constraints.NotNull;

/**
 * Service to handle changes to state and lifecycle of a game.
 */
public interface GameService {
    /**
     * Creates a new game without any players
     *
     * @return new game
     */
    Game createGame() throws GameError.ExistAlready;

    /**
     * Fetches the current state of the game for given game ID
     *
     * @param gameId game identifier
     * @return Existing game
     * @throws GameError.NotFoundException if game is not found
     */
    Game getGame(@NotNull String gameId) throws GameError.NotFoundException;

    /**
     * Registration of a new player to a running game
     *
     * @param gameId     game requested to enter
     * @param playerName desired player name to register to game
     * @return new player identifier
     * @throws GameError.NotFoundException     if game is not found
     * @throws GameError.FullCapacityException if game has no available seats
     */
    String enterGame(@NotNull String gameId, @NotNull String playerName) throws GameError.NotFoundException, GameError.FullCapacityException;

    /**
     * Request to leave a game
     *
     * @param gameId   game requested to enter
     * @param playerId player identifier
     * @throws GameError.NotFoundException if game is not found
     */
    void leaveGame(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException;

    /**
     * Player requested current round information to be able to submit propositions
     *
     * @param gameId   game requested to enter
     * @param playerId player requesting round
     * @return new or existing round
     * @throws GameError.NotFoundException      if game is not found
     * @throws RoundError.IllegalStateException if there are not enough players anymore
     */
    Round getRoundOpenForPropositions(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException;

    /**
     * Player requested round that has all selections already submitted
     *
     * @param gameId   game requested to enter
     * @param playerId player requesting round
     * @return new or existing round
     * @throws GameError.NotFoundException      if game is not found
     * @throws RoundError.IllegalStateException if there are not enough players anymore
     */
    Round getRoundClosedForSelections(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException, RoundError.IllegalStateException;

    /**
     * Player requested to participate in the current round of the game. If round is currently not available
     * a new one will be created
     *
     * @param gameId   game identifier
     * @param playerId player identifier
     * @throws GameError.NotFoundException   if game is not found
     * @throws PlayerError.NotFoundException if player is not found
     */
    void enterRound(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException, PlayerError.NotFoundException;

}
