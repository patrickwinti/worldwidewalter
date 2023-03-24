package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.util.HashMap;
import java.util.Map;


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
    private Map<String, Player> waitingRoom = new HashMap<>();
    private Map<String, Player> activePlayers = new HashMap<>();
}
