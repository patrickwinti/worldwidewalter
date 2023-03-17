package ch.zhaw.www.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * All exceptions related to round  and round state
 */
public abstract class RoundError extends RuntimeException {
    private RoundError(String message) {
        super(message);
    }

    /**
     * Round is in wrong state to be started
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class OngoingException extends RoundError {
        public OngoingException() {
            super("Round is ongoing and cannot start a new one");
        }
    }

    /**
     * Round ID passed has not been found
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFoundException extends RoundError {
        /**
         * Constructor for exception with message
         *
         * @param roundId roundId for clarification in the exception
         */
        public NotFoundException(@NotNull String roundId) {
            super(String.format("Round with ID = %s could not be found", roundId));
        }
    }
}
