package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import jakarta.validation.constraints.NotNull;

/**
 * Service to handle changes to state and lifecycle of a game.
 */
public interface GameService {
    /**
     * Creates a new game and registers the creator as its first player and host.
     *
     * @param hostName desired name of the host (creator)
     * @return new game with the host registered
     */
    Game createGame(@NotNull String hostName) throws GameError.ExistAlready;

    /**
     * Starts the game so the first round can begin. Only the host may start, and only once
     * enough players are present.
     *
     * @param gameId   game identifier
     * @param playerId player requesting the start (must be the host)
     * @return the started game
     * @throws GameError.NotFoundException         if game is not found
     * @throws GameError.NotHostException          if the player is not the host
     * @throws GameError.NotEnoughPlayersException if fewer than the minimum players are present
     */
    Game startGame(@NotNull String gameId, @NotNull String playerId)
            throws GameError.NotFoundException, GameError.NotHostException, GameError.NotEnoughPlayersException;

    /**
     * Reassigns the host to a random present player if the current host is no longer present
     * (left, or disconnected beyond the grace period) and the game has not started yet.
     * Best-effort and only touches the game when a reassignment actually happens.
     *
     * @param gameId game identifier
     */
    void reassignHostIfAbsent(@NotNull String gameId);
    
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
     * @return new player
     * @throws GameError.NotFoundException     if game is not found
     * @throws GameError.FullCapacityException if game has no available seats
     */
    Player enterGame(@NotNull String gameId, @NotNull String playerName) throws GameError.NotFoundException, GameError.FullCapacityException;
    
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
     * Player requested to participate in the current round of the game. If round is currently not available a new one
     * will be created
     *
     * @param gameId   game identifier
     * @param playerId player identifier
     * @throws GameError.NotFoundException   if game is not found
     * @throws PlayerError.NotFoundException if player is not found
     */
    void enterRound(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException, PlayerError.NotFoundException;

    /**
     * Marks a player as disconnected. The player remains in the game and can rejoin.
     *
     * @param gameId   game identifier
     * @param playerId player identifier
     */
    void disconnectPlayer(@NotNull String gameId, @NotNull String playerId);

    /**
     * Marks a player as connected again. Best-effort: does nothing if the game no longer exists.
     * Used when a WebSocket (re)connects so a recovered connection clears the disconnect grace timer.
     *
     * @param gameId   game identifier
     * @param playerId player identifier
     */
    void connectPlayer(@NotNull String gameId, @NotNull String playerId);

    /**
     * Rejoins an existing player to a game after disconnection
     *
     * @param gameId   game identifier
     * @param playerId player identifier
     * @return the rejoining player
     * @throws GameError.NotFoundException   if game is not found
     * @throws PlayerError.NotFoundException if player is not found in the game
     */
    Player rejoinGame(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException, PlayerError.NotFoundException;

}
