package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;


/**
 * Model class with all the current state
 * of a game.
 */
@Getter
@KeySpace("running_games")
public class Game {
    @Id
    @NotNull
    String id;
}
