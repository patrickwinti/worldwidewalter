package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * Prompt Class stores a statement that has one or more "placeholder" words. These placeholders are to be completed
 * by the players' proposition.
 * The prompt stores the total number of placeholder in the statement,
 * and tracks whether it has been used before in the game.
 */
@Data
public class Prompt {
    @NotNull
    private final String statement;
    private final long numberOfPlaceholders;
    private boolean hasBeenUsed = false;
}
