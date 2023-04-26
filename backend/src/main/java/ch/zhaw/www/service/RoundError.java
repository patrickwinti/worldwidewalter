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

    /**
     * Round has not enough players os is currently running
     */
    @ResponseStatus(HttpStatus.TOO_EARLY)
    public static class IllegalStateException extends RoundError {
        public IllegalStateException() {
            super("Round has not enough players or is currently running");
        }
    }
    
    /**
     * Operation is not allowed. E.g. Sphinx cannot select a proposition.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class IllegalOperationException extends RoundError {
        public IllegalOperationException() {
            super("This operation is forbidden.");
        }
    }
}
