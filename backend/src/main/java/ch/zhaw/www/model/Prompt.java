package ch.zhaw.www.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.NotNull;

/**
 * Prompt Class stores a statement that has one or more "placeholder" words. These placeholders are to be completed
 * by the players' proposition.
 * The prompt stores the total number of placeholder in the statement,
 * and tracks whether it has been used before in the game.
 */
@Data
@RequiredArgsConstructor
public class Prompt {
    @NotNull
    private final String statement;
}
