package ch.zhaw.www.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Holder for all exceptions that are related
 * to the game.
 */
public abstract class GameException extends RuntimeException {
    GameException(String message) {
        super(message);
    }

    /**
     * Game has no room for a new player to be added.
     * Registrations cannot be accepted anymore
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class FullCapacity extends GameException {
        /**
         * Constructor for exception with message
         *
         * @param message message for clarification in the exception
         */
        public FullCapacity(String message) {
            super(message);
        }
    }

    /**
     * Exception for cases where there is no game for
     * the given ID
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFound extends GameException {
        /**
         * @param gameId gameId for clarification in the exception
         */
        public NotFound(@NotNull String gameId) {
            super(String.format("Game with ID = %s could not be found", gameId));
        }
    }
}
