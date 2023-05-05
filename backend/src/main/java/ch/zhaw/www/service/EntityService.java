package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Round;
import jakarta.validation.constraints.NotNull;

import java.util.function.Function;

/**
 * Game repository with protected access to game instances
 */
public interface EntityService {
    
    /**
     * Gets a read only instance of the game. This game can not be read and is subject to dirty reads.
     *
     * @param gameId the game required
     * @return an existing game
     * @throws GameError.NotFoundException if game does not exist
     */
    Game getGame(@NotNull String gameId) throws GameError.NotFoundException;
    
    /**
     * Fetches the game for the provided ID. Changes needed to the game state can be done through the edit operator.
     *
     * @param gameId the required game ID
     * @param editor the changes on the game
     * @throws GameError.NotFoundException if game does not exist
     */
    <T> T editGame(@NotNull String gameId, Function<Game, T> editor) throws GameError.NotFoundException;
    
    /**
     * Fetches game for given round ID
     *
     * @param roundId round id that needs to be found
     * @return game with round given identifier
     * @throws RoundError.NotFoundException if no round is found matching the ID
     */
    Game getGameForRound(String roundId);
    
    /**
     * Fetches the round for given round ID
     *
     * @param roundId round id that needs to be found
     * @return round for given identifier
     * @throws RoundError.NotFoundException if no round is found matching the ID
     */
    Round getRound(@NotNull String roundId) throws RoundError.NotFoundException;
    
    /**
     * Checks if player is active for given round id
     *
     * @param roundId round id that needs to be found
     * @return if player is active otherwise false
     * @throws RoundError.NotFoundException if no round is found matching the ID
     */
    boolean isPlayerActiveInRound(@NotNull String roundId, @NotNull String playerId) throws RoundError.NotFoundException;
    
    /**
     * Edits round with given round ID
     *
     * @param roundId round id that needs to be found
     * @param editor  for changes in round
     * @throws RoundError.NotFoundException if no round is found matching the ID
     */
    <T> T editRound(@NotNull String roundId, Function<Round, T> editor) throws RoundError.NotFoundException;
    
    /**
     * Saves a new game. If game is already saved it will throw an exception.
     *
     * @param game to be saved
     */
    void saveNewGame(@NotNull Game game) throws GameError.ExistAlready;
    
}
