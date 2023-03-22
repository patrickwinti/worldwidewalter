package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Model class with all the current state
 * of a game.
 */
@AllArgsConstructor
@Data
public class Game {
    @NotNull
    String id;
}
