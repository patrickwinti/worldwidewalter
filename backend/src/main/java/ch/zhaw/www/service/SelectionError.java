package ch.zhaw.www.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * All exceptions related to selections
 */
public abstract class SelectionError extends RuntimeException {
    private SelectionError(String message) {
        super(message);
    }

    /**
     * Round ID passed has not been found
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class IllegalSelection extends SelectionError {
        /**
         * Constructor for exception with message
         *
         * @param propositionId propositionId for clarification in the exception
         */
        public IllegalSelection(@NotNull String propositionId) {
            super(String.format("Selection of proposition with ID = %s is not valid", propositionId));
        }
    }
}
