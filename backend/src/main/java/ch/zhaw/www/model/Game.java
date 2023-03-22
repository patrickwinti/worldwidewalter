package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;


/**
 * Model class with all the current state
 * of a game.
 */
@RequiredArgsConstructor
@Getter
@KeySpace("running_games")
public class Game {
    @Id
    @NotNull
    private final String id;
}
