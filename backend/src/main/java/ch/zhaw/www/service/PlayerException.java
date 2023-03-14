package ch.zhaw.www.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * All exceptions related to players
 */
public abstract class PlayerException extends RuntimeException {
    /**
     * Player ID passed has not been found
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFound extends GameException {
        /**
         * Constructor for exception with message
         *
         * @param playerId playerId for clarification in the exception
         */
        public NotFound(@NotNull String playerId) {
            super(String.format("Player with ID = %s could not be found", playerId));
        }
    }
}
