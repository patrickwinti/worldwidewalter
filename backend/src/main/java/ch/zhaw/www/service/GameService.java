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

    Round startNextRound(@NotNull String gameId) throws GameException.NotFound, RoundException.Ongoing, GameException.NotEnoughPlayers;

    void submitProposition(@NotNull String roundId, @NotNull String playerId, @NotNull String text) throws GameException.NotFound, RoundException.NotFound, PlayerException.NotFound;

    void selectProposition(@NotNull String roundId, @NotNull String playerId, @NotNull String propositionId) throws GameException.NotFound, RoundException.NotFound, PlayerException.NotFound;
}
