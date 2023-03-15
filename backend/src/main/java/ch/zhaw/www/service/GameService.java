package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Service to handle changes to state
 * and lifecycle of a game.
 */
public interface GameService {
    /**
     * Creates a new game without any players
     *
     * @return new game
     */
    Game createGame();

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
     * @throws GameError.NotFoundException if game is not found
     * @throws GameError.FullCapacity      if game has no available seats
     */
    Player enterGame(@NotNull String gameId, @NotNull String playerName) throws GameError.NotFoundException, GameError.FullCapacity;

    /**
     * Request to leave a game
     *
     * @param gameId   game requested to enter
     * @param playerId player identifier
     * @throws GameError.NotFoundException if game is not found
     */
    void leaveGame(@NotNull String gameId, @NotNull String playerId) throws GameError.NotFoundException;

    /**
     * Player requested to start round.
     *
     * @param gameId game requested to enter
     * @return new or existing round
     * @throws GameError.NotFoundException         if game is not found
     * @throws RoundError.OngoingException         if round can not be started
     * @throws GameError.NotEnoughPlayersException if there are not enough players anymore
     */
    Round startNextRound(@NotNull String gameId) throws GameError.NotFoundException, RoundError.OngoingException, GameError.NotEnoughPlayersException;

    /**
     * Player submitted their propositions for the prompt
     *
     * @param roundId  round identifier
     * @param playerId player identifier
     * @throws GameError.NotFoundException   if game is not found
     * @throws RoundError.NotFoundException  if round is not found
     * @throws PlayerError.NotFoundException if player is not found
     */
    void submitProposition(@NotNull String roundId, @NotNull String playerId, @NotNull List<String> gaps) throws GameError.NotFoundException, RoundError.NotFoundException, PlayerError.NotFoundException;

    /**
     * Player has chosen a proposition id. The given player id is anonymized and only valid for the round
     *
     * @param roundId       round identifier
     * @param playerId      player identifier
     * @param propositionId proposition identifier
     * @throws GameError.NotFoundException   if game is not found
     * @throws RoundError.NotFoundException  if round is not found
     * @throws PlayerError.NotFoundException if player is not found
     */
    void selectProposition(@NotNull String roundId, @NotNull String playerId, @NotNull String propositionId) throws GameError.NotFoundException, RoundError.NotFoundException, PlayerError.NotFoundException;
}
