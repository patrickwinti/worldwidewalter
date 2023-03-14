package ch.zhaw.www.models;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Model class with all the current state
 * of a game.
 */
@AllArgsConstructor
@Data
public class Game {
    @Nonnull
    String id;
}
