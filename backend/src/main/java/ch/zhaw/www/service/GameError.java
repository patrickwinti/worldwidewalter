package ch.zhaw.www.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Holder for all exceptions that are related to the game.
 */
public abstract class GameError extends RuntimeException {
    GameError(String message) {
        super(message);
    }

    /**
     * Game has no room for a new player to be added. Registrations cannot be accepted anymore
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class FullCapacityException extends GameError {
        public FullCapacityException() {
            super("Game it at capacity");
        }
    }

    /**
     * Game with given ID already exists
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class ExistAlready extends GameError {
        public ExistAlready() {
            super("Game already exists");
        }
    }

    /**
     * Exception for cases where there is no game for the given ID
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFoundException extends GameError {
        /**
         * @param gameId gameId for clarification in the exception
         */
        public NotFoundException(@NotNull String gameId) {
            super(String.format("Game with ID = %s could not be found", gameId));
        }
    }

    /**
     * A non-host player attempted a host-only action (e.g. starting the game).
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class NotHostException extends GameError {
        public NotHostException() {
            super("Only the host can perform this action");
        }
    }

    /**
     * The host tried to start the game with fewer than the minimum number of players present.
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class NotEnoughPlayersException extends GameError {
        public NotEnoughPlayersException() {
            super("Not enough players to start the game");
        }
    }

    /**
     * Another player in the game already uses the requested name.
     */
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static class NameTakenException extends GameError {
        /**
         * @param playerName the name that is already in use
         */
        public NameTakenException(@NotNull String playerName) {
            super(String.format("Name %s is already taken in this game", playerName));
        }
    }

    /**
     * The game has been ended by the host, so no further round can be played.
     */
    @ResponseStatus(HttpStatus.GONE)
    public static class EndedException extends GameError {
        public EndedException() {
            super("Game has been ended by the host");
        }
    }

    /**
     * A round action was attempted before the host started the game.
     */
    @ResponseStatus(HttpStatus.TOO_EARLY)
    public static class NotStartedException extends GameError {
        public NotStartedException() {
            super("Game has not been started by the host yet");
        }
    }
}
