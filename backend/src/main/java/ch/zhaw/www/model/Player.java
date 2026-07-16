package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Model class with basic
 * information of the player
 */
@Data
public class Player {
    @NotNull
    private String id;
    private String name;
    private boolean connected = true;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
