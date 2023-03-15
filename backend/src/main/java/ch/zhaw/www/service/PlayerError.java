package ch.zhaw.www.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * All exceptions related to players
 */
public abstract class PlayerError extends RuntimeException {
    /**
     * Player ID passed has not been found
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFoundException extends GameError {
        /**
         * Constructor for exception with message
         *
         * @param playerId playerId for clarification in the exception
         */
        public NotFoundException(@NotNull String playerId) {
            super(String.format("Player with ID = %s could not be found", playerId));
        }
    }
}
