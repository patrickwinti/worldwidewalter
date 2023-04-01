package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Model class with basic
 * information of the player
 */
@AllArgsConstructor
@Data
public class Player {
    @NotNull
    private String id;
    private String name;
}
