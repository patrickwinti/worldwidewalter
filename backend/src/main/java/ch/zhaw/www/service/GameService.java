package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import jakarta.validation.constraints.NotNull;

/**
 * Service to handle changes to state
 * and lifecycle of a game.
 */
public interface GameService {
    Game createGame();

    Player registerUser(@NotNull String gameId, @NotNull String playerName) throws GameException.NotFound, GameException.FullCapacity;
}
