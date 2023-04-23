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
     * Selector has selected his own proposition and no double proposition has been submitted by the selector
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class IllegalSelection extends SelectionError {
        /**
         * Constructor for exception with message
         *
         * @param propositionId for clarification in the exception
         */
        public IllegalSelection(@NotNull String propositionId) {
            super(String.format("Selection of proposition with ID = %s is not valid", propositionId));
        }
    }

    /**
     * Selector is Sphinx
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class IllegalSelector extends SelectionError {
        /**
         * Constructor for exception with message
         *
         * @param selectorId for clarification in the exception
         */
        public IllegalSelector(@NotNull String selectorId) {
            super(String.format("Id of selector: %s belongs to Sphinx", selectorId));
        }
    }
}
