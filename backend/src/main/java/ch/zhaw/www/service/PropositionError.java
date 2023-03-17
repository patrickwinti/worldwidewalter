package ch.zhaw.www.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * All exceptions related to propositions
 */
public abstract class PropositionError extends RuntimeException {
    private PropositionError(String message) {
        super(message);
    }

    /**
     * Proposition ID passed has not been found
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFoundException extends PropositionError {
        /**
         * Constructor for exception with message
         *
         * @param proposition proposition for clarification in the exception
         */
        public NotFoundException(@NotNull String proposition) {
            super(String.format("Proposition with ID = %s could not be found", proposition));
        }
    }
}
