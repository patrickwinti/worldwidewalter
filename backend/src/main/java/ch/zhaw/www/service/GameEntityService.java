package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import jakarta.validation.constraints.NotNull;

import java.util.function.UnaryOperator;

/**
 * Game repository with protected access to game instances
 */
public interface GameEntityService {
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
    void editGame(@NotNull String gameId, UnaryOperator<Game> editor) throws GameError.NotFoundException;
    
    /**
     * Fetches the game for given round ID
     *
     * @param roundId round id that needs to be found
     * @return game for given round
     * @throws RoundError.NotFoundException if no round is found matching the ID
     */
    Game getGameForRound(String roundId) throws RoundError.NotFoundException;
    
    /**
     * Fetches the game for given round ID
     *
     * @param roundId round id that needs to be found
     * @param editor  for changes in game
     * @throws RoundError.NotFoundException if no round is found matching the ID
     */
    void editGameForRound(String roundId, UnaryOperator<Game> editor) throws RoundError.NotFoundException;
    
    /**
     * Saves a new game. If game is already saved it will throw an exception.
     *
     * @param game to be saved
     */
    void saveNewGame(Game game);
}
