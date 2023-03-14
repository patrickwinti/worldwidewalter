package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import jakarta.validation.constraints.NotNull;

/**
 * Service to handle changes to state
 * and lifecycle of a game.
 */
public interface GameService {
    Game createGame();

    Player enterGame(@NotNull String gameId, @NotNull String playerName) throws GameException.NotFound, GameException.FullCapacity;

    void leaveGame(@NotNull String gameId, @NotNull String playerName) throws GameException.NotFound;

    Round startNextRound(String gameId) throws GameException.NotFound, GameException.RoundOngoing, GameException.NotEnoughPlayers;

}
